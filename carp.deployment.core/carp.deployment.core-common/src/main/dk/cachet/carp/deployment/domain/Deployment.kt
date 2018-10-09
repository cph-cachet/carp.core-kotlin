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
    private val _protocol = StudyProtocol.fromSnapshot( protocolSnapshot )


    init
    {
        // Verify whether protocol can be deployed.
        if ( !_protocol.isDeployable() )
        {
            throw IllegalArgumentException( "The passed protocol snapshot contains deployment errors." )
        }

        // TODO: Create status for each master device which needs to be registered.
        // TODO: What does registration entail?
    }


    /**
     * Get the status (serializable) of this [Deployment].
     */
    fun getStatus(): DeploymentStatus
    {
        return DeploymentStatus( id )
    }
}