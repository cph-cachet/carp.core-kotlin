package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Describes the status of a [Deployment]: registered devices, last received data, whether consent has been given, etc.
 */
@Serializable
data class DeploymentStatus( val deploymentId: UUID )