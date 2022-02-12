package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLoggingProxy
import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot


/**
 * A proxy for a protocol factory [service] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests in [loggedRequests].
 */
class ProtocolFactoryServiceLoggingProxy(
    service: ProtocolFactoryService,
    eventBus: EventBus,
    log: (LoggedRequest<ProtocolFactoryService, ProtocolFactoryService.Event>) -> Unit = { }
) :
    ApplicationServiceLoggingProxy<ProtocolFactoryService, ProtocolFactoryService.Event>(
        service,
        EventBusLog(
            eventBus,
            EventBusLog.Subscription( ProtocolFactoryService::class, ProtocolFactoryService.Event::class )
        ),
        log
    ),
    ProtocolFactoryService
{
    override suspend fun createCustomProtocol(
        ownerId: UUID,
        name: String,
        customProtocol: String,
        description: String?
    ): StudyProtocolSnapshot =
        log( ProtocolFactoryServiceRequest.CreateCustomProtocol( ownerId, name, customProtocol, description ) )
}
