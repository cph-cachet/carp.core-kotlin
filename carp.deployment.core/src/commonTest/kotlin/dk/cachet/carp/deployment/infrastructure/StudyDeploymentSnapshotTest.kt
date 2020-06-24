package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployment.domain.createComplexDeployment
import dk.cachet.carp.deployment.domain.studyDeploymentFor
import dk.cachet.carp.protocols.domain.devices.CustomDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.makeUnknown
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

        val allRegistrations = parsed.deviceRegistrationHistory.values.flatten()
        assertEquals( 1, allRegistrations.filterIsInstance<CustomDeviceRegistration>().count() )
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
     * Create a serialized deployment (snapshot) of a study protocol with exactly one master device which is registered using an unknown device registration.
     */
    private fun serializeDeploymentSnapshotIncludingUnknownRegistration(): String
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Stub" )
        protocol.addMasterDevice( master )
        val deployment = studyDeploymentFor( protocol )
        val registration = DefaultDeviceRegistration( "0" )
        deployment.registerDevice( master, registration )

        var serialized: String = deployment.getSnapshot().toJson()
        serialized = serialized.makeUnknown( registration )

        return serialized
    }
}
