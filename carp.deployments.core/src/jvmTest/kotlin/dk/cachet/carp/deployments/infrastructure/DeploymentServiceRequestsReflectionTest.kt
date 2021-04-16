package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.deployments.application.DeploymentService
import kotlin.test.*


/**
 * Tests for [DeploymentServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class DeploymentServiceRequestsReflectionTest
{
    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun request_object_for_each_request_available()
    {
        val serviceFunctions = DeploymentService::class.members
            .filterNot { it.name == "equals" || it.name == "hashCode" || it.name == "toString" }
        val testedRequests = DeploymentServiceRequestsTest.requests.map {
            val serviceInvoker = it as ServiceInvoker<DeploymentService, *>
            serviceInvoker.function
        }

        assertTrue( testedRequests.containsAll( serviceFunctions ) )
    }
}
