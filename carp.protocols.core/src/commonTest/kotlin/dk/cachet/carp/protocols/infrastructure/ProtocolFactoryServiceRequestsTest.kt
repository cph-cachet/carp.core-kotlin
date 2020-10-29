package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.UUID
import kotlin.test.*


/**
 * Tests for [ProtocolFactoryServiceRequest]'s.
 */
class ProtocolFactoryServiceRequestsTest
{
    companion object
    {
        val requests: List<ProtocolFactoryServiceRequest> = listOf(
            ProtocolFactoryServiceRequest.CreateCustomProtocol( UUID.randomUUID(), "Name", "...", "Description" )
        )
    }


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { request ->
            val serializer = ProtocolFactoryServiceRequest.serializer()
            val serialized = JSON.encodeToString( serializer, request )
            val parsed = JSON.decodeFromString( serializer, serialized )
            assertEquals( request, parsed )
        }
    }
}
