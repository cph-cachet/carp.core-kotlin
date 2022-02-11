package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest


val DeploymentServiceApiMigrator = ApplicationServiceApiMigrator(
    DeploymentService.API_VERSION,
    DeploymentServiceRequest.Serializer
)
