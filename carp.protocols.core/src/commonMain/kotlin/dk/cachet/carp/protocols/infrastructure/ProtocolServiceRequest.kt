package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.ProtocolVersion
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import kotlinx.serialization.Serializable


/**
 * Serializable application service requests to [ProtocolService] which can be executed on demand.
 */
@Serializable
sealed class ProtocolServiceRequest
{
    @Serializable
    data class Add( val protocol: StudyProtocolSnapshot, val versionTag: String = "Initial" ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, Unit> by createServiceInvoker( ProtocolService::add, protocol, versionTag )

    @Serializable
    data class Update( val protocol: StudyProtocolSnapshot, val versionTag: String = DateTime.now().toString() ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, Unit> by createServiceInvoker( ProtocolService::update, protocol, versionTag )

    @Serializable
    data class GetBy( val owner: ProtocolOwner, val protocolName: String, val versionTag: String? = null ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, StudyProtocolSnapshot> by createServiceInvoker( ProtocolService::getBy, owner, protocolName, versionTag )

    @Serializable
    data class GetAllFor( val owner: ProtocolOwner ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, List<StudyProtocolSnapshot>> by createServiceInvoker( ProtocolService::getAllFor, owner )

    @Serializable
    data class GetVersionHistoryFor( val owner: ProtocolOwner, val protocolName: String ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, List<ProtocolVersion>> by createServiceInvoker( ProtocolService::getVersionHistoryFor, owner, protocolName )
}
