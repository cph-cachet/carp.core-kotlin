package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHostTest
import dk.cachet.carp.deployments.application.DeploymentServiceTest
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceDecorator


class OutputDeploymentServiceTestRequests :
    OutputTestRequests<DeploymentService>( DeploymentService::class ),
    DeploymentServiceTest
{
    override fun createSUT(): DeploymentServiceTest.SUT
    {
        val (service, eventBus) = DeploymentServiceHostTest.createSUT()

        val (loggedService, logger) = createLoggedApplicationService(
            service,
            ::DeploymentServiceDecorator,
            EventBusLog(
                eventBus,
                EventBusLog.Subscription( DeploymentService::class, DeploymentService.Event::class )
            )
        )

        serviceLogger = logger

        return DeploymentServiceTest.SUT( loggedService, eventBus )
    }
}
