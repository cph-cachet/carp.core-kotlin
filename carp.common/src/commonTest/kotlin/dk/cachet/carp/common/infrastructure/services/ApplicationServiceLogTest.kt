package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequestTest.TestServiceRequest as TestServiceRequest
import kotlin.test.*


/**
 * Tests for [ApplicationServiceLog] relying on core infrastructure.
 */
class ApplicationServiceLogTest
{
    @Test
    fun can_serialize_and_deserialize_LoggedRequest_Succeeded()
    {
        val request: LoggedRequest<*> = LoggedRequest.Succeeded(
            request = TestServiceRequest.Operation( 42 ),
            response = 42
        )

        val json = createDefaultJSON()
        val serializer = LoggedRequestSerializer( TestServiceRequest.Serializer )
        val serialized = json.encodeToString( serializer, request )
        val parsed = json.decodeFromString( serializer, serialized )

        assertEquals( request, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_LoggedRequest_Failed()
    {
        val request: LoggedRequest<*> = LoggedRequest.Failed(
            request = TestServiceRequest.Operation( 10 ),
            exceptionType = IllegalArgumentException::class.simpleName!!
        )

        val json = createDefaultJSON()
        val serializer = LoggedRequestSerializer( TestServiceRequest.Serializer )
        val serialized = json.encodeToString( serializer, request )
        val parsed = json.decodeFromString( serializer, serialized )

        assertEquals( request, parsed )
    }
}
