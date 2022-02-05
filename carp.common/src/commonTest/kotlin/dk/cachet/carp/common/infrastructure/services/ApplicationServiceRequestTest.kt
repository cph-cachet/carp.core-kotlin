package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [ApplicationServiceRequest].
 */
class ApplicationServiceRequestTest
{
    interface TestService : ApplicationService<TestService, TestService.Event>
    {
        @Serializable
        sealed class Event : IntegrationEvent<TestService>

        suspend fun operation( parameter: Int ): Int
    }

    @Serializable
    sealed class TestServiceRequest<out TReturn> : ApplicationServiceRequest<TestService, TReturn>
    {
        object Serializer : KSerializer<TestServiceRequest<*>> by ignoreTypeParameters( ::serializer )

        @Serializable
        data class Operation( val parameter: Int ) : TestServiceRequest<Int>()
        {
            override suspend fun invokeOn( service: TestService ): Int = service.operation( parameter )
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

    @Test
    fun invokeOn_requests_calls_service() = runSuspendTest {
        var requestResponse: Int? = null
        val service =
            object : TestService
            {
                override suspend fun operation( parameter: Int ): Int = parameter.also { requestResponse = it }
            }

        val request = TestServiceRequest.Operation( 42 )
        request.invokeOn( service )

        assertEquals( 42, requestResponse )
    }

    @Test
    fun invokeOn_deserialized_request_succeeds() = runSuspendTest {
        var requestResponse: Int? = null
        val service =
            object : TestService
            {
                override suspend fun operation( parameter: Int ): Int = parameter.also { requestResponse = it }
            }

        val serializedRequest =
            Json.encodeToString( TestServiceRequest.Serializer, TestServiceRequest.Operation( 42 ) )
        val request = Json.decodeFromString( TestServiceRequest.Serializer, serializedRequest )
        request.invokeOn( service )

        assertEquals( 42, requestResponse )
    }
}
