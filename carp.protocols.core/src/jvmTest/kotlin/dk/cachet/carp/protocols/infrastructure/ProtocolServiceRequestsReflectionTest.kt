package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.protocols.application.ProtocolService
import kotlin.test.*


/**
 * Tests for [ProtocolServiceRequest]'s.
 */
class ProtocolServiceRequestsReflectionTest
{
    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun request_object_for_each_request_available()
    {
        val serviceFunctions = ProtocolService::class.members
            .filterNot { it.name == "equals" || it.name == "hashCode" || it.name == "toString" }
        val testedRequests = ProtocolServiceRequestsTest.requests.map {
            val serviceInvoker = it as ServiceInvoker<ProtocolService, *>
            serviceInvoker.function
        }

        assertTrue( testedRequests.containsAll( serviceFunctions ) )
    }
}