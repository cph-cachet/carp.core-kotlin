package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [ApplicationServiceRequest].
 */
class ApplicationServiceRequestTest
{
    interface TestService : ApplicationService<TestService, TestService.Event>
    {
        companion object { val API_VERSION = ApiVersion( 1, 0 ) }

        @Serializable
        sealed class Event( override val aggregateId: String? ) : IntegrationEvent<TestService>
        {
            @Required
            override val apiVersion: ApiVersion = API_VERSION

            @Serializable
            data class OperationOccurred( val parameter: Int ) : Event( parameter.toString() )
        }

        suspend fun operation( parameter: Int ): Int
    }

    @Serializable
    sealed class TestServiceRequest<out TReturn> : ApplicationServiceRequest<TestService, TReturn>()
    {
        @Required
        override val apiVersion: ApiVersion = TestService.API_VERSION
        object Serializer : KSerializer<TestServiceRequest<*>> by ignoreTypeParameters( ::serializer )

        @Serializable
        data class Operation( val parameter: Int ) : TestServiceRequest<Int>()
        {
            override fun getResponseSerializer() = serializer<Int>()
        }
    }


    @Test
    fun can_serialize_and_deserialize_request()
    {
        val request = TestServiceRequest.Operation( 42 )
        val serialized = Json.encodeToString( request )
        val parsed = Json.decodeFromString<TestServiceRequest.Operation>( serialized )

        assertEquals( request, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_polymorphic_request()
    {
        val request: TestServiceRequest<Any?> = TestServiceRequest.Operation( 42 )
        val serialized = Json.encodeToString( TestServiceRequest.Serializer, request )
        val parsed: TestServiceRequest<*> = Json.decodeFromString( TestServiceRequest.Serializer, serialized )

        assertEquals( request, parsed )
    }
}
