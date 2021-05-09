package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.services.ApplicationServiceEventBus
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.users.ParticipantRepository
import dk.cachet.carp.studies.domain.users.Recruitment
import dk.cachet.carp.studies.application.users.participantIds


// TODO: Participant data is currently retrieved through `participationService` for individual service call.
//  Instead, we need to subscribe to events from this service and copy the data locally.
class RecruitmentServiceHost(
    private val participantRepository: ParticipantRepository,
    private val deploymentService: DeploymentService,
    private val participationService: ParticipationService,
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
                val idsToRemove = recruitment.participations.keys
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
     * Deploy the study with the given [studyId] to a [group] of previously added participants.
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
    override suspend fun deployParticipantGroup( studyId: UUID, group: Set<AssignParticipantDevices> ): ParticipantGroupStatus
    {
        val recruitment = getRecruitmentOrThrow( studyId )
        val recruitmentStatus = recruitment.verifyReadyForDeployment( group )

        // In case the same participants have been invited before,
        // and that deployment is still running, return the existing group.
        // TODO: The same participants might be invited for different role names, which we currently cannot differentiate between.
        val toDeployParticipantIds = group.map { it.participantId }.toSet()
        val deployedStatus = recruitment.participations.entries
            .firstOrNull { (_, participations) ->
                participations.map { it.id }.toSet() == toDeployParticipantIds
            }
            ?.let { deploymentService.getStudyDeploymentStatus( it.key ) }
        if ( deployedStatus != null && deployedStatus !is StudyDeploymentStatus.Stopped )
        {
            val participants = recruitment.getParticipations( deployedStatus.studyDeploymentId )
            val participantData = participationService.getParticipantData( deployedStatus.studyDeploymentId )
            return ParticipantGroupStatus( deployedStatus, participants, participantData.data )
        }

        // Get participant information.
        val allParticipants = recruitment.participants.associateBy { it.id }
        require( group.participantIds().all { it in allParticipants } )
            { "One of the specified participants is not part of this study." }

        // Create deployment and add participations to study.
        // TODO: How to deal with failing or partially succeeding requests?
        //       In a distributed setup, deploymentService would be network calls.
        val deploymentStatus = deploymentService.createStudyDeployment( recruitmentStatus.studyProtocol )
        for ( toAssign in group )
        {
            val identity: AccountIdentity = allParticipants.getValue( toAssign.participantId ).accountIdentity
            participationService.addParticipation(
                deploymentStatus.studyDeploymentId,
                toAssign.participantId,
                toAssign.masterDeviceRoleNames,
                identity,
                recruitmentStatus.invitation )

            recruitment.addParticipation( allParticipants[ toAssign.participantId ]!!, deploymentStatus.studyDeploymentId )
        }

        participantRepository.updateRecruitment( recruitment )

        val participants = recruitment.getParticipations( deploymentStatus.studyDeploymentId )
        val participantData = participationService.getParticipantData( deploymentStatus.studyDeploymentId )
        return ParticipantGroupStatus( deploymentStatus, participants, participantData.data )
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
        val studyDeploymentIds = recruitment.participations.keys
        val studyDeploymentStatuses: List<StudyDeploymentStatus> =
            if ( studyDeploymentIds.isEmpty() ) emptyList()
            else deploymentService.getStudyDeploymentStatusList( studyDeploymentIds )

        // Map each study deployment status to a deanonymized participant group status.
        val participantDataList = participationService.getParticipantDataList( studyDeploymentIds )
        return studyDeploymentStatuses.map { deployment ->
            val participants = recruitment.getParticipations( deployment.studyDeploymentId )
            val participantData = participantDataList.first { it.studyDeploymentId == deployment.studyDeploymentId }
            ParticipantGroupStatus( deployment, participants, participantData.data )
        }
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
        val participations = getParticipationsOrThrow( studyId, groupId )

        val deploymentStatus = deploymentService.stop( groupId )
        val participantData = participationService.getParticipantData( deploymentStatus.studyDeploymentId )
        return ParticipantGroupStatus( deploymentStatus, participations, participantData.data )
    }

    /**
     * Set participant [data] for the given [inputDataType],
     * related to participants of the participant group with [groupId] in the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when:
     *   - a study with [studyId] or participant group with [groupId] does not exist.
     *   - [inputDataType] is not configured as expected participant data in the study protocol
     *   - [data] is invalid data for [inputDataType]
     */
    override suspend fun setParticipantGroupData( studyId: UUID, groupId: UUID, inputDataType: InputDataType, data: Data? ): ParticipantGroupStatus
    {
        val participations = getParticipationsOrThrow( studyId, groupId )

        val deploymentStatus = deploymentService.getStudyDeploymentStatus( groupId )
        val newData = participationService.setParticipantData( groupId, inputDataType, data )
        return ParticipantGroupStatus( deploymentStatus, participations, newData.data )
    }

    private suspend fun getParticipationsOrThrow( studyId: UUID, groupId: UUID ): Set<Participant>
    {
        val recruitment: Recruitment = getRecruitmentOrThrow( studyId )
        val participations = recruitment.participations[ groupId ]
        requireNotNull( participations ) { "Study deployment with the specified groupId not found." }

        return participations
    }

    private suspend fun getRecruitmentOrThrow( studyId: UUID ): Recruitment = participantRepository.getRecruitment( studyId )
        ?: throw IllegalArgumentException( "Study with ID \"$studyId\" not found." )
}
