package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.common.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceMock
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [DeploymentServiceRequest]'s.
 */
class DeploymentServiceRequestsTest
{
    companion object
    {
        val requests: List<DeploymentServiceRequest> = listOf(
            DeploymentServiceRequest.CreateStudyDeployment( createEmptyProtocol().getSnapshot() ),
            DeploymentServiceRequest.RemoveStudyDeployments( emptySet() ),
            DeploymentServiceRequest.GetStudyDeploymentStatus( UUID.randomUUID() ),
            DeploymentServiceRequest.GetStudyDeploymentStatusList( setOf( UUID.randomUUID() ) ),
            DeploymentServiceRequest.RegisterDevice( UUID.randomUUID(), "Test role", DefaultDeviceRegistration( "Device ID" ) ),
            DeploymentServiceRequest.UnregisterDevice( UUID.randomUUID(), "Test role" ),
            DeploymentServiceRequest.GetDeviceDeploymentFor( UUID.randomUUID(), "Test role" ),
            DeploymentServiceRequest.DeploymentSuccessful( UUID.randomUUID(), "Test role", DateTime.now() ),
            DeploymentServiceRequest.Stop( UUID.randomUUID() )
        )
    }

    private val mock = DeploymentServiceMock()


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { request ->
            val serializer = DeploymentServiceRequest.serializer()
            val serialized = JSON.encodeToString( serializer, request )
            val parsed = JSON.decodeFromString( serializer, serialized )
            assertEquals( request, parsed )
        }
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun invokeOn_requests_call_service() = runSuspendTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<DeploymentService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }
}
