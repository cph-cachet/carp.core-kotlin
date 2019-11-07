package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.ddd.ApplicationServiceRequest
import dk.cachet.carp.protocols.application.*
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.test.runBlockingTest
import kotlinx.serialization.*
import kotlin.reflect.*
import kotlin.test.*


/**
 * Tests for [ProtocolServiceRequests].
 */
class ProtocolServiceRequestsTest
{
    companion object {
        private val requests: Map<ApplicationServiceRequest<ProtocolService, *>, KCallable<*>> = mapOf(
            ProtocolServiceRequests.Add(createComplexProtocol().getSnapshot(), "Initial") to ProtocolService::add,
            ProtocolServiceRequests.Update(createComplexProtocol().getSnapshot(), "Updated") to ProtocolService::update,
            ProtocolServiceRequests.GetBy(ProtocolOwner(), "Name", "Version") to ProtocolService::getBy,
            ProtocolServiceRequests.GetAllFor(ProtocolOwner()) to ProtocolService::getAllFor,
            ProtocolServiceRequests.GetVersionHistoryFor(ProtocolOwner(), "Name") to ProtocolService::getVersionHistoryFor
        )
        private val serializers: Map<KClass<*>, SerializationStrategy<*>> = mapOf(
            ProtocolServiceRequests.Add::class to ProtocolServiceRequests.Add.serializer(),
            ProtocolServiceRequests.Update::class to ProtocolServiceRequests.Update.serializer(),
            ProtocolServiceRequests.GetBy::class to ProtocolServiceRequests.GetBy.serializer(),
            ProtocolServiceRequests.GetAllFor::class to ProtocolServiceRequests.GetAllFor.serializer(),
            ProtocolServiceRequests.GetVersionHistoryFor::class to ProtocolServiceRequests.GetVersionHistoryFor.serializer()
        )
    }

    private val mock = ProtocolServiceMock()


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { (request, _) ->
            val serializer = serializers[ request::class ] ?:
                error( "No matching serializer registered for request type." )
            val serialized = JSON.stringify( serializer as KSerializer<Any>, request )
            val parsed = JSON.parse( serializer, serialized )
            assertEquals( request, parsed )
        }
    }

    @Test
    fun executeOn_requests_call_service() = runBlockingTest {
        requests.forEach { (request, function) ->
            request.executeOn( mock )
            assertTrue( mock.wasCalled( function ) )
            mock.reset()
        }
    }
}