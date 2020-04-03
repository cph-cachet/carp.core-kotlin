package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.createComplexDeployment
import dk.cachet.carp.deployment.domain.createEmptyProtocol
import dk.cachet.carp.deployment.domain.StubMasterDeviceDescriptor
import dk.cachet.carp.deployment.domain.STUBS_SERIAL_MODULE
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.studyDeploymentFor
import dk.cachet.carp.deployment.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployment.domain.UnknownDeviceRegistration
import dk.cachet.carp.deployment.domain.UnknownMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.infrastructure.fromJson
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
    fun creating_study_deployment_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val deployment = createComplexDeployment()

        val snapshot = deployment.getSnapshot()
        val fromSnapshot = StudyDeployment.fromSnapshot( snapshot )

        assertEquals( deployment.id, fromSnapshot.id )
        assertEquals( deployment.protocolSnapshot, fromSnapshot.protocolSnapshot )
        val commonRegisteredDevices =
            deployment.registeredDevices.asIterable().intersect( fromSnapshot.registeredDevices.asIterable() )
        assertEquals( deployment.registeredDevices.count(), commonRegisteredDevices.count() )
        val commonParticipations =
            deployment.participations.intersect( fromSnapshot.participations )
        assertEquals( deployment.participations.count(), commonParticipations.count() )
        val commonDeployedDevices =
            deployment.deployedDevices.intersect( fromSnapshot.deployedDevices )
        assertEquals( deployment.deployedDevices.count(), commonDeployedDevices.count() )
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
