package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.deployment.application.ParticipationService
import kotlin.test.*


/**
 * Tests for [ParticipationServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class ParticipationServiceRequestsReflectionTest
{
    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun request_object_for_each_request_available()
    {
        val serviceFunctions = ParticipationService::class.members
            .filterNot { it.name == "equals" || it.name == "hashCode" || it.name == "toString" }
        val testedRequests = ParticipationServiceRequestsTest.requests.map {
            val serviceInvoker = it as ServiceInvoker<ParticipationService, *>
            serviceInvoker.function
        }

        assertTrue( testedRequests.containsAll( serviceFunctions ) )
    }
}
