package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.*


/**
 * A single instantiation of a [StudyProtocol], taking care of common concerns when 'running' a study.
 *
 * I.e., a [Deployment] is responsible for registering the physical devices described in the [StudyProtocol],
 * enabling a connection between them, tracking device connection issues, assessing data quality,
 * and registering participant consent.
 */
class Deployment( protocolSnapshot: StudyProtocolSnapshot, val id: UUID = UUID.randomUUID() )
{
    companion object Factory
    {
        fun fromStatus( protocolSnapshot: StudyProtocolSnapshot, status: DeploymentStatus ): Deployment
        {
            return Deployment( protocolSnapshot, status.deploymentId )
        }
    }


    private val _protocol = StudyProtocol.fromSnapshot( protocolSnapshot )


    /**
     * Get the status (serializable) of this [Deployment].
     */
    fun getStatus(): DeploymentStatus
    {
        return DeploymentStatus.fromDeployment( this )
    }
}