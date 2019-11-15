package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.*
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.infrastructure.*
import kotlin.test.*


/**
 * Tests for [StudyDeployment] relying on core infrastructure.
 */
class StudyDeploymentTest
{
    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createDeploymentSerializer( STUBS_SERIAL_MODULE )
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
            StudyDeployment( invalidSnapshot, testId )
        }
    }

    @Test
    fun create_deployment_fromSnapshot_with_custom_extending_types_succeeds()
    {
        val protocol = createEmptyProtocol()
        val master = UnknownMasterDeviceDescriptor( "Unknown" )
        protocol.addMasterDevice( master )
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( master, UnknownDeviceRegistration( "0" ) )

        var serialized: String = deployment.getSnapshot().toJson()
        serialized = serialized.replace( "dk.cachet.carp.deployment.domain.UnknownMasterDeviceDescriptor", "com.unknown.CustomMasterDevice" )
        serialized = serialized.replace( "dk.cachet.carp.deployment.domain.UnknownDeviceRegistration", "com.unknown.CustomDeviceRegistration" )

        val snapshot = StudyDeploymentSnapshot.fromJson( serialized )
        StudyDeployment.fromSnapshot( snapshot )
    }
}