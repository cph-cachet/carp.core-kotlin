package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.createComplexDeployment
import dk.cachet.carp.deployment.domain.createEmptyProtocol
import dk.cachet.carp.deployment.domain.studyDeploymentFor
import dk.cachet.carp.deployment.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployment.domain.STUBS_SERIAL_MODULE
import dk.cachet.carp.deployment.domain.UnknownDeviceRegistration
import dk.cachet.carp.deployment.domain.UnknownMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.CustomDeviceRegistration
import kotlin.test.*


/**
 * Tests for [StudyDeploymentSnapshot] relying on core infrastructure.
 */
class StudyDeploymentSnapshotTest
{
    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createDeploymentSerializer( STUBS_SERIAL_MODULE )
    }

    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON()
    {
        val deployment = createComplexDeployment()
        val snapshot: StudyDeploymentSnapshot = deployment.getSnapshot()

        val serialized: String = snapshot.toJson()
        val parsed: StudyDeploymentSnapshot = StudyDeploymentSnapshot.fromJson( serialized )

        assertEquals( snapshot, parsed )
    }

    /**
     * Types not known at compile time should not prevent deserializing a deployment, but should be loaded through a 'Custom' type wrapper.
     */
    @Test
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val serialized = serializeDeploymentSnapshotIncludingUnknownRegistration()
        val parsed = StudyDeploymentSnapshot.fromJson( serialized )

        assertEquals( 1, parsed.registeredDevices.values.filterIsInstance<CustomDeviceRegistration>().count() )
    }

    /**
     * Types which were wrapped in a 'Custom' type wrapper upon deserialization should be serialized to their original form (returning the original type, not the wrapper).
     */
    @Test
    fun serializing_unknown_types_removes_the_wrapper()
    {
        val serialized: String = serializeDeploymentSnapshotIncludingUnknownRegistration()
        val snapshot = StudyDeploymentSnapshot.fromJson( serialized )

        val customSerialized = snapshot.toJson()
        assertEquals( serialized, customSerialized )
    }

    /**
     * Create a serialized deployment (snapshot) of a study protocol with exactly one master device which is registered using an [UnknownDeviceRegistration].
     */
    private fun serializeDeploymentSnapshotIncludingUnknownRegistration(): String
    {
        val protocol = createEmptyProtocol()
        val master = UnknownMasterDeviceDescriptor( "Stub" )
        protocol.addMasterDevice( master )
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( master, UnknownDeviceRegistration( "0" ) )

        var serialized: String = deployment.getSnapshot().toJson()
        serialized = serialized.replace( "dk.cachet.carp.deployment.domain.UnknownDeviceRegistration", "com.unknown.CustomDeviceRegistration" )

        return serialized
    }
}
