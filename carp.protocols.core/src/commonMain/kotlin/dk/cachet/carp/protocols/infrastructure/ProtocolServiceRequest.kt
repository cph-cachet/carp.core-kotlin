package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.common.users.ParticipantAttribute
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.domain.ProtocolVersion
import dk.cachet.carp.protocols.domain.StudyProtocol
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
    data class AddVersion( val protocol: StudyProtocolSnapshot, val versionTag: String = DateTime.now().toString() ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, Unit> by createServiceInvoker( ProtocolService::addVersion, protocol, versionTag )

    @Serializable
    data class UpdateParticipantDataConfiguration( val protocolId: StudyProtocol.Id, val versionTag: String, val expectedParticipantData: Set<ParticipantAttribute> ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, StudyProtocolSnapshot> by createServiceInvoker( ProtocolService::updateParticipantDataConfiguration, protocolId, versionTag, expectedParticipantData )

    @Serializable
    data class GetBy( val protocolId: StudyProtocol.Id, val versionTag: String? = null ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, StudyProtocolSnapshot> by createServiceInvoker( ProtocolService::getBy, protocolId, versionTag )

    @Serializable
    data class GetAllFor( val ownerId: UUID ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, List<StudyProtocolSnapshot>> by createServiceInvoker( ProtocolService::getAllFor, ownerId )

    @Serializable
    data class GetVersionHistoryFor( val protocolId: StudyProtocol.Id ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, List<ProtocolVersion>> by createServiceInvoker( ProtocolService::getVersionHistoryFor, protocolId )
}
