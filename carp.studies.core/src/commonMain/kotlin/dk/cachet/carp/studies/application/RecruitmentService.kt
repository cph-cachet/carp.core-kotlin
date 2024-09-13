package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.DependentServices
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import kotlinx.serialization.*


/**
 * Application service which allows setting recruitment goals,
 * adding participants to studies, and creating deployments for them.
 */
@DependentServices( StudyService::class )
interface RecruitmentService : ApplicationService<RecruitmentService, RecruitmentService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 2 ) }

    @Serializable
    sealed class Event : IntegrationEvent<RecruitmentService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }


    /**
     * Add a [Participant] to the study with the specified [studyId], identified by the specified [email] address.
     * In case the [email] was already added before, the same [Participant] is returned.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun addParticipant( studyId: UUID, email: EmailAddress ): Participant

    /**
     * Add a [Participant] to the study with the specified [studyId], identified by the specified [username].
     * In case the [username] was already added before, the same [Participant] is returned.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun addParticipant( studyId: UUID, username: Username ): Participant

    /**
     * Returns a participant of a study with the specified [studyId], identified by [participantId].
     *
     * @throws IllegalArgumentException when a study with [studyId] or participant with [participantId] does not exist.
     */
    suspend fun getParticipant( studyId: UUID, participantId: UUID ): Participant

    /**
     * Get all [Participant]s for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun getParticipants( studyId: UUID ): List<Participant>

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
     *  - any of the participant roles specified in [group] does not exist
     *  - not all necessary participant roles part of the study have been assigned a participant
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    suspend fun inviteNewParticipantGroup( studyId: UUID, group: Set<AssignedParticipantRoles> ): ParticipantGroupStatus

    /**
     * Create a new participant [group] of previously added participants for the study with the given [studyId].
     * This is used to create a group of participants which can be deployed at a later time.
     *
     * In case a group with the same participants has already been deployed and is still running (not stopped),
     * the latest status for this group is simply returned.
     *
     * @throws IllegalArgumentException when:
     *  - a study with [studyId] does not exist
     *  - [group] is empty
     *  - any of the participant roles specified in [group] does not exist
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    suspend fun createParticipantGroup( studyId: UUID, group: Set<AssignedParticipantRoles> ): ParticipantGroupStatus

    /**
     * Update the participant [newGroup] with the specified [groupId] in the study with the given [studyId].
     * This can be used to add or remove participants from the group.
     *
     * @throws IllegalArgumentException when a study with [studyId] or participant group with [groupId] does not exist.
     * [newGroup] is empty
     * - any of the participant roles specified in [newGroup] does not exist
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    suspend fun updateParticipantGroup(
        studyId: UUID,
        groupId: UUID,
        newGroup: Set<AssignedParticipantRoles>
    ): ParticipantGroupStatus

    /**
     * Invite the participant group with the specified [groupId] (equivalent to the studyDeploymentId)
     * in the study with the given [studyId] to start participating in the study.
     *
     * @throws IllegalArgumentException when a study with [studyId] or participant group with [groupId] does not exist.
     *  - any of the participant roles specified in this participant group does not exist
     *  - not all necessary participant roles part of the study have been assigned a participant
     */
    suspend fun inviteParticipantGroup(studyId: UUID, groupId: UUID ): ParticipantGroupStatus

    /**
     * Get the status of all deployed participant groups in the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun getParticipantGroupStatusList( studyId: UUID ): List<ParticipantGroupStatus>

    /**
     * Stop the study deployment in the study with the given [studyId]
     * of the participant group with the specified [groupId] (equivalent to the studyDeploymentId).
     * No further changes to this deployment will be allowed and no more data will be collected.
     *
     * @throws IllegalArgumentException when a study with [studyId] or participant group with [groupId] does not exist.
     */
    suspend fun stopParticipantGroup( studyId: UUID, groupId: UUID ): ParticipantGroupStatus
}
