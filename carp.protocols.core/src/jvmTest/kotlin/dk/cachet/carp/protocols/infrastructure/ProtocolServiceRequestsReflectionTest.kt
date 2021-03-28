package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.infrastructure.ServiceInvoker
import dk.cachet.carp.protocols.application.ProtocolService
import kotlin.test.*


/**
 * Tests for [ProtocolServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
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
