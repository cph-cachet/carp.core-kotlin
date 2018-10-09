package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.*
import kotlin.test.*


/**
 * Tests for [Deployment].
 */
class DeploymentTest
{
    @Test
    fun cant_initialize_deployment_with_errors()
    {
        val protocol = createEmptyProtocol() // Does not contain a master device, thus contains deployment error.
        val snapshot = protocol.getSnapshot()

        assertFailsWith<IllegalArgumentException>
        {
            Deployment( snapshot, UUID( "27c56423-b7cd-48dd-8b7f-f819621a34f0" ) )
        }
    }

    @Test
    fun getStatus_of_new_deployment()
    {
        val protocol = createSingleMasterDeviceProtocol()
        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        val deployment = Deployment( snapshot, UUID( "27c56423-b7cd-48dd-8b7f-f819621a34f0" ) )

        val status: DeploymentStatus = deployment.getStatus()

        assertEquals( deployment.id, status.deploymentId )
    }
}