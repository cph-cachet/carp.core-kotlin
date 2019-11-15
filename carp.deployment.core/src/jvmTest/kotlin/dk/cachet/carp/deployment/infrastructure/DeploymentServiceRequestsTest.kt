package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.deployment.application.*
import dk.cachet.carp.deployment.domain.createEmptyProtocol
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [DeploymentServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class DeploymentServiceRequestsTest
{
    companion object {
        val requests: List<DeploymentServiceRequest> = listOf(
            DeploymentServiceRequest.CreateStudyDeployment( createEmptyProtocol().getSnapshot() ),
            DeploymentServiceRequest.GetStudyDeploymentStatus( UUID.randomUUID() ),
            DeploymentServiceRequest.RegisterDevice( UUID.randomUUID(), "Test role", DefaultDeviceRegistration( "Device ID" ) ),
            DeploymentServiceRequest.GetDeviceDeploymentFor( UUID.randomUUID(), "Test role" )
        )
    }

    private val mock = DeploymentServiceMock()


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { request ->
            val serializer = DeploymentServiceRequest.serializer()
            val serialized = JSON.stringify( serializer, request )
            val parsed = JSON.parse( serializer, serialized )
            assertEquals( request, parsed )
        }
    }

    @Test
    fun executeOn_requests_call_service() = runBlockingTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<DeploymentService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }

    @Test
    fun request_object_for_each_request_available()
    {
        val serviceFunctions = DeploymentService::class.members
            .filterNot { it.name == "equals" || it.name == "hashCode" || it.name == "toString" }
        val testedRequests = DeploymentServiceRequestsTest.requests.map {
            val serviceInvoker = it as ServiceInvoker<ProtocolService, *>
            serviceInvoker.function
        }

        assertTrue( testedRequests.containsAll( serviceFunctions ) )
    }
}