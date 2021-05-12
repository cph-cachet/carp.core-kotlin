package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceMock
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol


/**
 * Tests for [DeploymentServiceRequest]'s.
 */
class DeploymentServiceRequestsTest : ApplicationServiceRequestsTest<DeploymentService, DeploymentServiceRequest>(
    DeploymentService::class,
    DeploymentServiceMock(),
    DeploymentServiceRequest.serializer(),
    REQUESTS
)
{
    companion object
    {
        private val REQUESTS: List<DeploymentServiceRequest> = listOf(
            DeploymentServiceRequest.CreateStudyDeployment( UUID.randomUUID(), createEmptyProtocol().getSnapshot(), listOf() ),
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
}
