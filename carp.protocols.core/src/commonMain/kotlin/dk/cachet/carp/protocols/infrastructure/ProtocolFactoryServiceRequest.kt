package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import kotlinx.serialization.Serializable


/**
 * Serializable application service requests to [ProtocolFactoryService] which can be executed on demand.
 */
@Serializable
sealed class ProtocolFactoryServiceRequest
{
    @Serializable
    data class CreateCustomProtocol( val ownerId: UUID, val name: String, val customProtocol: String, val description: String ) :
        ProtocolFactoryServiceRequest(),
        ServiceInvoker<ProtocolFactoryService, StudyProtocolSnapshot> by createServiceInvoker( ProtocolFactoryService::createCustomProtocol, ownerId, name, customProtocol, description )
}
