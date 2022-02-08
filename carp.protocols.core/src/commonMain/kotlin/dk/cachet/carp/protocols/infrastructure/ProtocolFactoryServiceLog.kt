package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot


/**
 * A proxy for a protocol factory [service] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests in [loggedRequests].
 */
class ProtocolFactoryServiceLog(
    service: ProtocolFactoryService,
    log: (LoggedRequest<ProtocolFactoryService>) -> Unit = { }
) : ApplicationServiceLog<ProtocolFactoryService>( ProtocolFactoryService::class, service, log ),
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
