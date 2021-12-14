package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.common.infrastructure.services.createServiceInvoker
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.datetime.Clock
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
    data class AddVersion( val protocol: StudyProtocolSnapshot, val versionTag: String = Clock.System.now().toString() ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, Unit> by createServiceInvoker( ProtocolService::addVersion, protocol, versionTag )

    @Serializable
    data class UpdateParticipantDataConfiguration(
        val protocolId: UUID,
        val versionTag: String,
        val expectedParticipantData: Set<ParticipantAttribute>
    ) : ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, StudyProtocolSnapshot> by createServiceInvoker( ProtocolService::updateParticipantDataConfiguration, protocolId, versionTag, expectedParticipantData )

    @Serializable
    data class GetBy( val protocolId: UUID, val versionTag: String? = null ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, StudyProtocolSnapshot> by createServiceInvoker( ProtocolService::getBy, protocolId, versionTag )

    @Serializable
    data class GetAllForOwner( val ownerId: UUID ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, List<StudyProtocolSnapshot>> by createServiceInvoker( ProtocolService::getAllForOwner, ownerId )

    @Serializable
    data class GetVersionHistoryFor( val protocolId: UUID ) :
        ProtocolServiceRequest(),
        ServiceInvoker<ProtocolService, List<ProtocolVersion>> by createServiceInvoker( ProtocolService::getVersionHistoryFor, protocolId )
}
