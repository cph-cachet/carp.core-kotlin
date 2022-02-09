package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLoggingProxy
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot


/**
 * A proxy for a protocol factory [service] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests in [loggedRequests] and published events in [loggedEvents].
 */
class ProtocolFactoryServiceLoggingProxy(
    service: ProtocolFactoryService,
    log: (LoggedRequest<ProtocolFactoryService, ProtocolFactoryService.Event>) -> Unit = { }
) :
    ApplicationServiceLoggingProxy<ProtocolFactoryService, ProtocolFactoryService.Event>(
        service,
        ProtocolFactoryService::class,
        ProtocolFactoryService.Event::class,
        SingleThreadedEventBus(),
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
