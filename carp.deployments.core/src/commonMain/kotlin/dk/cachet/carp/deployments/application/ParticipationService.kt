package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.DependentServices
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.ParticipantData
import kotlinx.serialization.*


/**
 * Application service which allows retrieving participations for study deployments,
 * and managing data related to participants which is input by users.
 */
@DependentServices( DeploymentService::class )
interface ParticipationService : ApplicationService<ParticipationService, ParticipationService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 0 ) }

    @Serializable
    sealed class Event : IntegrationEvent<ParticipationService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }


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
     * Set [data] that was [inputByParticipantRole] in the study deployment with [studyDeploymentId],
     * or unset it by passing `null`.
     * When you want to set data that was assigned to a specific participant role,
     * [inputByParticipantRole] needs to be set.
     * You can still set common data (assigned to all roles) in the same call.
     *
     * @throws IllegalArgumentException when:
     *   - there is no study deployment with [studyDeploymentId]
     *   - one or more of the keys in [data] isn't configured as expected participant data in the study protocol
     *   - one or more of the set [data] isn't valid for the corresponding input data type
     * @return All data for the specified study deployment, including the newly set data.
     */
    suspend fun setParticipantData(
        studyDeploymentId: UUID,
        data: Map<InputDataType, Data?>,
        /**
         * The participant role who filled out [data]; null if all roles can set it.
         */
        inputByParticipantRole: String? = null
    ): ParticipantData
}
