package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.*
import dk.cachet.carp.protocols.domain.*


/**
 * Application service which allows instantiating [StudyProtocol]'s as [Deployment]'s.
 */
class DeploymentManager( private val repository: DeploymentRepository )
{
    /**
     * Instantiate a deployment for a given [StudyProtocolSnapshot].
     *
     * @throws InvalidConfigurationError when [protocol] is invalid.
     */
    fun createDeployment( protocol: StudyProtocolSnapshot ): DeploymentStatus
    {
        val newDeployment = Deployment( protocol )
        repository.add( newDeployment )

        return newDeployment.getStatus()
    }

    /**
     * Get the status for a [Deployment] with the given [deploymentId].
     *
     * @deploymentId The id of the [Deployment] to return [DeploymentStatus] for.
     */
    fun getDeploymentStatus( deploymentId: UUID ): DeploymentStatus
    {
        val deployment = repository.getBy( deploymentId )

        return deployment.getStatus()
    }
}