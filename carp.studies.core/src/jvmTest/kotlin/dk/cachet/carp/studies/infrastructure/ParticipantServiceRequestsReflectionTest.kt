package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.infrastructure.ServiceInvoker
import dk.cachet.carp.studies.application.ParticipantService
import kotlin.test.*


/**
 * Tests for [ParticipantServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class ParticipantServiceRequestsReflectionTest
{
    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun request_object_for_each_request_available()
    {
        val serviceFunctions = ParticipantService::class.members
            .filterNot { it.name == "equals" || it.name == "hashCode" || it.name == "toString" }
        val testedRequests = ParticipantServiceRequestsTest.requests.map {
            val serviceInvoker = it as ServiceInvoker<ParticipantService, *>
            serviceInvoker.function
        }

        assertTrue( testedRequests.containsAll( serviceFunctions ) )
    }
}
