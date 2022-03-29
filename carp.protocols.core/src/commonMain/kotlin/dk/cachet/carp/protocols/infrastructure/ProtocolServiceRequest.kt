package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.application.users.ExpectedParticipantData
import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer


/**
 * Serializable application service requests to [ProtocolService] which can be executed on demand.
 */
@Serializable
sealed class ProtocolServiceRequest<out TReturn> : ApplicationServiceRequest<ProtocolService, TReturn>
{
    @Required
    override val apiVersion: ApiVersion = ProtocolService.API_VERSION

    object Serializer : KSerializer<ProtocolServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class Add( val protocol: StudyProtocolSnapshot, val versionTag: String = "Initial" ) :
        ProtocolServiceRequest<Unit>()
    {
        override fun getResponseSerializer() = serializer<Unit>()
        override suspend fun invokeOn( service: ProtocolService ) = service.add( protocol, versionTag )
    }

    @Serializable
    data class AddVersion( val protocol: StudyProtocolSnapshot, val versionTag: String = Clock.System.now().toString() ) :
        ProtocolServiceRequest<Unit>()
    {
        override fun getResponseSerializer() = serializer<Unit>()
        override suspend fun invokeOn( service: ProtocolService ) = service.addVersion( protocol, versionTag )
    }

    @Serializable
    data class UpdateParticipantDataConfiguration(
        val protocolId: UUID,
        val versionTag: String,
        val expectedParticipantData: Set<ExpectedParticipantData>
    ) : ProtocolServiceRequest<StudyProtocolSnapshot>()
    {
        override fun getResponseSerializer() = serializer<StudyProtocolSnapshot>()
        override suspend fun invokeOn( service: ProtocolService ) =
            service.updateParticipantDataConfiguration( protocolId, versionTag, expectedParticipantData )
    }

    @Serializable
    data class GetBy( val protocolId: UUID, val versionTag: String? = null ) :
        ProtocolServiceRequest<StudyProtocolSnapshot>()
    {
        override fun getResponseSerializer() = serializer<StudyProtocolSnapshot>()
        override suspend fun invokeOn( service: ProtocolService ) = service.getBy( protocolId, versionTag )
    }

    @Serializable
    data class GetAllForOwner( val ownerId: UUID ) : ProtocolServiceRequest<List<StudyProtocolSnapshot>>()
    {
        override fun getResponseSerializer() = serializer<List<StudyProtocolSnapshot>>()
        override suspend fun invokeOn( service: ProtocolService ) = service.getAllForOwner( ownerId )
    }

    @Serializable
    data class GetVersionHistoryFor( val protocolId: UUID ) : ProtocolServiceRequest<List<ProtocolVersion>>()
    {
        override fun getResponseSerializer() = serializer<List<ProtocolVersion>>()
        override suspend fun invokeOn( service: ProtocolService ) = service.getVersionHistoryFor( protocolId )
    }
}
