package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequestTest.TestService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequestTest.TestServiceRequest
import kotlin.test.*


/**
 * Tests for [ApplicationServiceLoggingProxy] relying on core infrastructure.
 */
class ApplicationServiceLoggingProxyTest
{
    @Test
    fun can_serialize_and_deserialize_LoggedRequest_Succeeded()
    {
        val request: LoggedRequest<*, *> = LoggedRequest.Succeeded(
            request = TestServiceRequest.Operation( 42 ),
            // Preceding events would normally be of different application services.
            precedingEvents = listOf( TestService.Event.OperationOccurred( 0 ) ),
            publishedEvents = listOf( TestService.Event.OperationOccurred( 42 ) ),
            response = 42
        )

        val json = createDefaultJSON()
        val serializer = LoggedRequestSerializer( TestServiceRequest.Serializer, TestService.Event.serializer() )
        val serialized = json.encodeToString( serializer, request )
        val parsed = json.decodeFromString( serializer, serialized )

        assertEquals( request, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_LoggedRequest_Failed()
    {
        val request: LoggedRequest<*, *> = LoggedRequest.Failed(
            request = TestServiceRequest.Operation( 10 ),
            // Preceding events would normally be of different application services.
            precedingEvents = listOf( TestService.Event.OperationOccurred( 0 ) ),
            publishedEvents = listOf( TestService.Event.OperationOccurred( 10 ) ),
            exceptionType = IllegalArgumentException::class.simpleName!!
        )

        val json = createDefaultJSON()
        val serializer = LoggedRequestSerializer( TestServiceRequest.Serializer, TestService.Event.serializer() )
        val serialized = json.encodeToString( serializer, request )
        val parsed = json.decodeFromString( serializer, serialized )

        assertEquals( request, parsed )
    }
}
