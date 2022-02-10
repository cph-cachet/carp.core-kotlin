package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLoggingProxy
import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.ParticipantData


/**
 * A proxy for a participation [service] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests in [loggedRequests] and published events in [loggedEvents].
 */
class ParticipationServiceLoggingProxy(
    service: ParticipationService,
    eventBus: EventBus,
    log: (LoggedRequest<ParticipationService, ParticipationService.Event>) -> Unit = { }
) :
    ApplicationServiceLoggingProxy<ParticipationService, ParticipationService.Event>(
        service,
        EventBusLog(
            eventBus,
            EventBusLog.Subscription( ParticipationService::class, ParticipationService.Event::class ),
        ),
        log
    ),
    ParticipationService
{
    override suspend fun getActiveParticipationInvitations( accountId: UUID ): Set<ActiveParticipationInvitation> =
        log( ParticipationServiceRequest.GetActiveParticipationInvitations( accountId ) )

    override suspend fun getParticipantData( studyDeploymentId: UUID ): ParticipantData =
        log( ParticipationServiceRequest.GetParticipantData( studyDeploymentId ) )

    override suspend fun getParticipantDataList( studyDeploymentIds: Set<UUID> ): List<ParticipantData> =
        log( ParticipationServiceRequest.GetParticipantDataList( studyDeploymentIds ) )

    override suspend fun setParticipantData(
        studyDeploymentId: UUID,
        data: Map<InputDataType, Data?>
    ): ParticipantData =
        log( ParticipationServiceRequest.SetParticipantData( studyDeploymentId, data ) )
}
