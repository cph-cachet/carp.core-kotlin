package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApplicationServiceEventBus
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.users.ParticipantRepository
import dk.cachet.carp.studies.domain.users.Recruitment


class RecruitmentServiceHost(
    private val participantRepository: ParticipantRepository,
    private val deploymentService: DeploymentService,
    private val eventBus: ApplicationServiceEventBus<RecruitmentService, RecruitmentService.Event>
) : RecruitmentService
{
    init
    {
        eventBus.subscribe {
            // Create a recruitment per study.
            event { created: StudyService.Event.StudyCreated ->
                val recruitment = Recruitment( created.study.studyId )
                participantRepository.addRecruitment( recruitment )
            }

            // Once a study goes live, its study protocol locks in and participant groups may be deployed.
            event { goneLive: StudyService.Event.StudyGoneLive ->
                val recruitment = participantRepository.getRecruitment( goneLive.study.studyId )
                checkNotNull( recruitment )
                checkNotNull( goneLive.study.protocolSnapshot )
                recruitment.lockInStudy( goneLive.study.protocolSnapshot, goneLive.study.invitation )
                participantRepository.updateRecruitment( recruitment )
            }

            // Propagate removal of all data related to a study.
            event { removed: StudyService.Event.StudyRemoved ->
                // Remove deployments in the deployments subsystem.
                val recruitment = participantRepository.getRecruitment( removed.studyId )
                checkNotNull( recruitment )
                val idsToRemove = recruitment.participantGroups.keys
                deploymentService.removeStudyDeployments( idsToRemove )

                participantRepository.removeStudy( removed.studyId )
            }
        }
    }


    /**
     * Add a [Participant] to the study with the specified [studyId], identified by the specified [email] address.
     * In case the [email] was already added before, the same [Participant] is returned.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun addParticipant( studyId: UUID, email: EmailAddress ): Participant
    {
        val recruitment = getRecruitmentOrThrow( studyId )

        val participant = recruitment.addParticipant( email )
        participantRepository.updateRecruitment( recruitment )

        return participant
    }

    /**
     * Returns a participant of a study with the specified [studyId], identified by [participantId].
     *
     * @throws IllegalArgumentException when a study with [studyId] or participant with [participantId] does not exist.
     */
    override suspend fun getParticipant( studyId: UUID, participantId: UUID ): Participant
    {
        val recruitment = getRecruitmentOrThrow( studyId )

        val participant = recruitment.participants.firstOrNull { it.id == participantId }
        requireNotNull( participant ) { "Participant with ID \"$participantId\" not found." }

        return participant
    }

    /**
     * Get all [Participant]s for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun getParticipants( studyId: UUID ): List<Participant> =
        getRecruitmentOrThrow( studyId ).participants.toList()

    /**
     * Create a new participant [group] of previously added participants and instantly send out invitations
     * to participate in the study with the given [studyId].
     *
     * In case a group with the same participants has already been deployed and is still running (not stopped),
     * the latest status for this group is simply returned.
     *
     * @throws IllegalArgumentException when:
     *  - a study with [studyId] does not exist
     *  - [group] is empty
     *  - any of the participants specified in [group] does not exist
     *  - any of the master device roles specified in [group] are not part of the configured study protocol
     *  - not all master devices part of the study have been assigned a participant
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    override suspend fun inviteNewParticipantGroup( studyId: UUID, group: Set<AssignParticipantDevices> ): ParticipantGroupStatus
    {
        val recruitment = getRecruitmentOrThrow( studyId )
        val (protocol, invitations) = recruitment.createInvitations( group )

        // In case the same participants have been invited before,
        // and that deployment is still running, return the existing group.
        // TODO: The same participants might be invited for different role names, which we currently cannot differentiate between.
        val toDeployParticipantIds = group.map { it.participantId }.toSet()
        val deployedStatus = recruitment.participantGroups.entries
            .firstOrNull { (_, group) ->
                group.participantIds == toDeployParticipantIds
            }
            ?.let { deploymentService.getStudyDeploymentStatus( it.key ) }
        if ( deployedStatus != null && deployedStatus !is StudyDeploymentStatus.Stopped )
        {
            return recruitment.getParticipantGroupStatus( deployedStatus )
        }

        // Create participant group, deploy, and send invitations.
        val participantGroup = recruitment.addParticipantGroup( toDeployParticipantIds )
        val deploymentStatus = deploymentService.createStudyDeployment( participantGroup.id, protocol, invitations )
        participantGroup.markAsInvited( deploymentStatus )

        participantRepository.updateRecruitment( recruitment )

        return recruitment.getParticipantGroupStatus( deploymentStatus )
    }

    /**
     * Get the status of all deployed participant groups in the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun getParticipantGroupStatusList( studyId: UUID ): List<ParticipantGroupStatus>
    {
        val recruitment: Recruitment = getRecruitmentOrThrow( studyId )

        // Get study deployment statuses.
        val studyDeploymentIds = recruitment.participantGroups.keys
        val studyDeploymentStatuses: List<StudyDeploymentStatus> =
            if ( studyDeploymentIds.isEmpty() ) emptyList()
            else deploymentService.getStudyDeploymentStatusList( studyDeploymentIds )

        return studyDeploymentStatuses.map { recruitment.getParticipantGroupStatus( it ) }
    }

    /**
     * Stop the study deployment in the study with the given [studyId]
     * of the participant group with the specified [groupId] (equivalent to the studyDeploymentId).
     * No further changes to this deployment will be allowed and no more data will be collected.
     *
     * @throws IllegalArgumentException when a study with [studyId] or participant group with [groupId] does not exist.
     */
    override suspend fun stopParticipantGroup( studyId: UUID, groupId: UUID ): ParticipantGroupStatus
    {
        val recruitment = getRecruitmentWithGroupOrThrow( studyId, groupId )

        val deploymentStatus = deploymentService.stop( groupId )
        return recruitment.getParticipantGroupStatus( deploymentStatus )
    }

    private suspend fun getRecruitmentWithGroupOrThrow( studyId: UUID, groupId: UUID ): Recruitment
    {
        val recruitment: Recruitment = getRecruitmentOrThrow( studyId )
        val participations = recruitment.participantGroups[ groupId ]
        requireNotNull( participations ) { "Study deployment with the specified groupId not found." }

        return recruitment
    }

    private suspend fun getRecruitmentOrThrow( studyId: UUID ): Recruitment = participantRepository.getRecruitment( studyId )
        ?: throw IllegalArgumentException( "Study with ID \"$studyId\" not found." )
}
