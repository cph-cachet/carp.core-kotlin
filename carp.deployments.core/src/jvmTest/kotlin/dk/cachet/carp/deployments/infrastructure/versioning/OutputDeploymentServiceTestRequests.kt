package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHostTest
import dk.cachet.carp.deployments.application.DeploymentServiceTest
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceLoggingProxy


class OutputDeploymentServiceTestRequests :
    OutputTestRequests<DeploymentService>( DeploymentService::class ),
    DeploymentServiceTest
{
    override fun createService(): DeploymentService
    {
        val services = DeploymentServiceHostTest.createService()

        return DeploymentServiceLoggingProxy( services.first, services.second )
            .also { loggedService = it }
    }
}
