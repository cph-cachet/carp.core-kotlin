package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.devices.CustomDeviceRegistration
import kotlin.test.*


/**
 * Tests for [DeploymentSnapshot].
 */
class DeploymentSnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = deploymentFor( protocol )
        val snapshot: DeploymentSnapshot = deployment.getSnapshot()


        val serialized: String = snapshot.toJson()
        val parsed: DeploymentSnapshot = DeploymentSnapshot.fromJson( serialized )

        assertEquals( snapshot, parsed )
    }

    /**
     * Types not known at compile time should not prevent deserializing a deployment, but should be loaded through a 'Custom' type wrapper.
     */
    @Test
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val serialized = serializeDeploymentSnapshotIncludingUnknownRegistration()
        val parsed = DeploymentSnapshot.fromJson( serialized )

        assertEquals( 1, parsed.registeredDevices.values.filter { m -> m is CustomDeviceRegistration }.count() )
    }

    /**
     * Types which were wrapped in a 'Custom' type wrapper upon deserialization should be serialized to their original form (returning the original type, not the wrapper).
     */
    @Test
    fun serializing_unknown_types_removes_the_wrapper()
    {
        val serialized: String = serializeDeploymentSnapshotIncludingUnknownRegistration()
        val snapshot = DeploymentSnapshot.fromJson( serialized )

        val customSerialized = snapshot.toJson()
        assertEquals( serialized, customSerialized )
    }

    /**
     * Create a serialized deployment (snapshot) of a study protocol with exactly one master device which is registered using an [UnknownDeviceRegistration].
     */
    private fun serializeDeploymentSnapshotIncludingUnknownRegistration(): String
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( master )
        val deployment = deploymentFor( protocol )
        deployment.registerDevice( master, UnknownDeviceRegistration( "0" ) )

        var serialized: String = deployment.getSnapshot().toJson()
        serialized = serialized.replace( "dk.cachet.carp.deployment.domain.UnknownDeviceRegistration", "com.unknown.CustomDeviceRegistration" )

        return serialized
    }
}