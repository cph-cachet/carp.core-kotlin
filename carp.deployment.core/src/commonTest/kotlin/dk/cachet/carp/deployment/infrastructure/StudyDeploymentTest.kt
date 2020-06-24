package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployment.domain.studyDeploymentFor
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.infrastructure.fromJson
import dk.cachet.carp.protocols.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.makeUnknown
import dk.cachet.carp.protocols.infrastructure.toJson
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
            StudyDeployment( invalidSnapshot )
        }
    }

    @Test
    fun create_deployment_fromSnapshot_with_custom_extending_types_succeeds()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Unknown" )
        protocol.addMasterDevice( master )
        val deployment = studyDeploymentFor( protocol )
        val registration = DefaultDeviceRegistration( "0" )
        deployment.registerDevice( master, registration )

        // Mimic unknown device and registration.
        var serialized: String = deployment.getSnapshot().toJson()
        serialized = serialized.makeUnknown( master, "Unknown" )
        serialized = serialized.makeUnknown( registration )

        val snapshot = StudyDeploymentSnapshot.fromJson( serialized )
        StudyDeployment.fromSnapshot( snapshot )
    }
}
