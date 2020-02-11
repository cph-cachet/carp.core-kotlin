package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.common.users.UsernameAccountIdentity
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceMock
import dk.cachet.carp.deployment.domain.createEmptyProtocol
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.test.runBlockingTest
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
            DeploymentServiceRequest.GetStudyDeploymentStatus( UUID.randomUUID() ),
            DeploymentServiceRequest.RegisterDevice( UUID.randomUUID(), "Test role", DefaultDeviceRegistration( "Device ID" ) ),
            DeploymentServiceRequest.GetDeviceDeploymentFor( UUID.randomUUID(), "Test role" ),
            DeploymentServiceRequest.AddParticipation( UUID.randomUUID(), UsernameAccountIdentity( "Test" ), StudyInvitation.empty() )
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

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun invokeOn_requests_call_service() = runBlockingTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<DeploymentService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }
}
