package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.*
import kotlin.test.*


/**
 * Tests for [Deployment].
 */
class DeploymentTest
{
    private val testId = UUID( "27c56423-b7cd-48dd-8b7f-f819621a34f0" )


    @Test
    fun cant_initialize_deployment_with_errors()
    {
        val protocol = createEmptyProtocol()
        val snapshot = protocol.getSnapshot()

        // Protocol does not contain a master device, thus contains deployment error and can't be initialized.
        assertFailsWith<IllegalArgumentException>
        {
            Deployment( snapshot, testId )
        }
    }

    @Test
    fun cant_initialize_deployment_with_invalid_snapshot()
    {
        // Initialize valid protocol.
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Master" )
        val connected = StubMasterDeviceDescriptor( "Connected" )
        protocol.addMasterDevice( master )
        protocol.addConnectedDevice( connected, master )
        val snapshot = protocol.getSnapshot()

        // Create invalid snapshot by editing JSON.
        val json = snapshot.toJson()
        val invalidJson = json.replaceFirst( "\"Master\"", "\"Non-existing device\"" )
        val invalidSnapshot = StudyProtocolSnapshot.fromJson( invalidJson )

        assertFailsWith<IllegalArgumentException>
        {
            Deployment( invalidSnapshot, testId )
        }
    }

    @Test
    fun getStatus_of_new_deployment()
    {
        val protocol = createSingleMasterDeviceProtocol()
        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        val deployment = Deployment( snapshot, testId )

        val status: DeploymentStatus = deployment.getStatus()

        assertEquals( deployment.id, status.deploymentId )
        // TODO: Verify whether the initial state is as expected. I.e., no devices registered.
    }
}