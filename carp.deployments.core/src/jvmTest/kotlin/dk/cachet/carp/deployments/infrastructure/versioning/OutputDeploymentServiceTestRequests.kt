package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHostTest
import dk.cachet.carp.deployments.application.DeploymentServiceTest
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceLoggingProxy


private val services = DeploymentServiceHostTest.createService()

class OutputDeploymentServiceTestRequests :
    OutputTestRequests<DeploymentService>(
        DeploymentService::class,
        DeploymentServiceLoggingProxy( services.first, services.second )
    ),
    DeploymentServiceTest
{
    override fun createService(): DeploymentService = loggedService
}
