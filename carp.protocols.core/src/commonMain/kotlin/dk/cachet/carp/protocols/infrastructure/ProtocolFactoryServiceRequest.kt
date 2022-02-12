package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer


/**
 * Serializable application service requests to [ProtocolFactoryService] which can be executed on demand.
 */
@Serializable
sealed class ProtocolFactoryServiceRequest<out TReturn> : ApplicationServiceRequest<ProtocolFactoryService, TReturn>
{
    @Required
    override val apiVersion: ApiVersion = ProtocolFactoryService.API_VERSION

    object Serializer : KSerializer<ProtocolFactoryServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class CreateCustomProtocol(
        val ownerId: UUID,
        val name: String,
        val customProtocol: String,
        val description: String?
    ) : ProtocolFactoryServiceRequest<StudyProtocolSnapshot>()
    {
        override fun getResponseSerializer() = serializer<StudyProtocolSnapshot>()
        override suspend fun invokeOn( service: ProtocolFactoryService ) =
            service.createCustomProtocol( ownerId, name, customProtocol, description )
    }
}
