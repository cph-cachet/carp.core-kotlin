package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.ActiveParticipationInvitation
import dk.cachet.carp.deployment.domain.users.ParticipantData
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import kotlinx.serialization.Serializable


/**
 * Application service which allows inviting participants, retrieving participations for study deployments,
 * and managing data related to participants which is input by users.
 */
interface ParticipationService : ApplicationService<ParticipationService, ParticipationService.Event>
{
    @Serializable
    sealed class Event : IntegrationEvent<ParticipationService>()


    /**
     * Let the person with the specified [identity] participate in the study deployment with [studyDeploymentId],
     * using the master devices with the specified [assignedMasterDeviceRoleNames].
     * In case no account is associated to the specified [identity], a new account is created.
     * An [invitation] (and account details) is delivered to the person managing the [identity],
     * or should be handed out manually to the relevant participant by the person managing the specified [identity].
     *
     * @throws IllegalArgumentException when:
     * - there is no study deployment with [studyDeploymentId]
     * - any of the [assignedMasterDeviceRoleNames] are not part of the study protocol deployment
     * @throws IllegalStateException when:
     * - the specified [identity] was already invited to participate in this deployment and a different [invitation] is specified than a previous request
     * - this deployment has stopped
     */
    suspend fun addParticipation(
        studyDeploymentId: UUID,
        assignedMasterDeviceRoleNames: Set<String>,
        identity: AccountIdentity,
        invitation: StudyInvitation
    ): Participation

    /**
     * Get all participations of active study deployments the account with the given [accountId] has been invited to.
     */
    suspend fun getActiveParticipationInvitations( accountId: UUID ): Set<ActiveParticipationInvitation>

    /**
     * Get currently set data for all expected participant data in the study deployment with [studyDeploymentId].
     * Data which is not set equals null.
     *
     * @throws IllegalArgumentException when there is no study deployment with [studyDeploymentId].
     */
    suspend fun getParticipantData( studyDeploymentId: UUID ): ParticipantData

    /**
     * Get currently set data for all expected participant data for a set of study deployments with [studyDeploymentIds].
     * Data which is not set equals null.
     *
     * @throws IllegalArgumentException when [studyDeploymentIds] contains an ID for which no deployment exists.
     */
    suspend fun getParticipantDataList( studyDeploymentIds: Set<UUID> ): List<ParticipantData>

    /**
     * Set participant [data] for the given [inputDataType] in the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     *   - there is no study deployment with [studyDeploymentId]
     *   - [inputDataType] is not configured as expected participant data in the study protocol
     *   - [data] is invalid data for [inputDataType]
     * @return All data for the specified study deployment, including the newly set data.
     */
    suspend fun setParticipantData( studyDeploymentId: UUID, inputDataType: InputDataType, data: Data? ): ParticipantData
}
