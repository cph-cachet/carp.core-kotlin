package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.ddd.*
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.*


/**
 * Serializable application service requests to [ProtocolService] which can be executed on demand.
 */
class ProtocolServiceRequests
{
    @Serializable
    data class Add( val protocol: StudyProtocolSnapshot, val versionTag: String = "Initial" )
        : ApplicationServiceRequest<ProtocolService, Unit> by createRequest( { add( protocol, versionTag ) } )

    @Serializable
    data class Update( val protocol: StudyProtocolSnapshot, val versionTag: String = DateTime.now().toString() )
        : ApplicationServiceRequest<ProtocolService, Unit> by createRequest( { update( protocol, versionTag ) } )

    @Serializable
    data class GetBy( val owner: ProtocolOwner, val protocolName: String, val versionTag: String? = null )
        : ApplicationServiceRequest<ProtocolService, StudyProtocolSnapshot> by createRequest( { getBy( owner, protocolName, versionTag ) } )

    @Serializable
    data class GetAllFor( val owner: ProtocolOwner )
        : ApplicationServiceRequest<ProtocolService, List<StudyProtocolSnapshot>> by createRequest( { getAllFor( owner ) } )

    @Serializable
    data class GetVersionHistoryFor( val owner: ProtocolOwner, val protocolName: String )
        : ApplicationServiceRequest<ProtocolService, List<ProtocolVersion>> by createRequest( { getVersionHistoryFor( owner, protocolName ) } )
}