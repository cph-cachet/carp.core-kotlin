package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.ApplicationService
import dk.cachet.carp.common.application.IntegrationEvent
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.ParticipantGroupStatus


/**
 * Application service which allows adding participants to studies and creating deployments for them.
 */
interface ParticipantService : ApplicationService<ParticipantService, ParticipantService.Event>
{
    sealed class Event : IntegrationEvent<ParticipantService>()


    /**
     * Add a [Participant] to the study with the specified [studyId], identified by the specified [email] address.
     * In case the [email] was already added before, the same [Participant] is returned.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun addParticipant( studyId: UUID, email: EmailAddress ): Participant

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
    suspend fun deployParticipantGroup( studyId: UUID, group: Set<AssignParticipantDevices> ): ParticipantGroupStatus

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

    /**
     * Set participant [data] for the given [inputDataType],
     * related to participants of the participant group with [groupId] in the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when:
     *   - a study with [studyId] or participant group with [groupId] does not exist.
     *   - [inputDataType] is not configured as expected participant data in the study protocol
     *   - [data] is invalid data for [inputDataType]
     */
    suspend fun setParticipantGroupData( studyId: UUID, groupId: UUID, inputDataType: InputDataType, data: Data? ): ParticipantGroupStatus
}
