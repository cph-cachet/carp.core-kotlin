package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHostTest
import dk.cachet.carp.deployments.application.DeploymentServiceTest
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceDecorator
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest


class OutputDeploymentServiceTestRequests :
    OutputTestRequests<DeploymentService, DeploymentService.Event, DeploymentServiceRequest<*>>(
        DeploymentService::class,
        ::DeploymentServiceDecorator
    ),
    DeploymentServiceTest
{
    override fun createSUT(): DeploymentServiceTest.SUT
    {
        val (service, eventBus) = DeploymentServiceHostTest.createSUT()
        val loggedService = createLoggedApplicationService( service, eventBus )

        return DeploymentServiceTest.SUT( loggedService, eventBus )
    }
}
