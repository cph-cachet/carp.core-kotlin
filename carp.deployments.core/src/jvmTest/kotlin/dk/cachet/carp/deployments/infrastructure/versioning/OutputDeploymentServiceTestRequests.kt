package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHostTest
import dk.cachet.carp.deployments.application.DeploymentServiceTest
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceLog


class OutputDeploymentServiceTestRequests :
    OutputTestRequests<DeploymentService>( DeploymentServiceLog( DeploymentServiceHostTest.createService() ) ),
    DeploymentServiceTest
{
    override fun createService(): DeploymentService = loggedService
}
