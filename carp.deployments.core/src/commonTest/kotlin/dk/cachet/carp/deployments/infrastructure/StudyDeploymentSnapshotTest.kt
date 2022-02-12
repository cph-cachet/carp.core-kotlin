package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.infrastructure.serialization.CustomDeviceRegistration
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import dk.cachet.carp.deployments.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployments.domain.createComplexDeployment
import dk.cachet.carp.deployments.domain.studyDeploymentFor
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [StudyDeploymentSnapshot] relying on core infrastructure.
 */
class StudyDeploymentSnapshotTest
{
    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createTestJSON()
    }

    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON()
    {
        val deployment = createComplexDeployment()
        val snapshot: StudyDeploymentSnapshot = deployment.getSnapshot()

        val serialized: String = JSON.encodeToString( snapshot )
        val parsed: StudyDeploymentSnapshot = JSON.decodeFromString( serialized )

        assertEquals( snapshot, parsed )
    }

    /**
     * Types not known at compile time should not prevent deserializing a deployment, but should be loaded through a 'Custom' type wrapper.
     */
    @ExperimentalSerializationApi
    @Test
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val serialized = serializeDeploymentSnapshotIncludingUnknownRegistration()
        val parsed: StudyDeploymentSnapshot = JSON.decodeFromString( serialized )

        val allRegistrations = parsed.deviceRegistrationHistory.values.flatten()
        assertEquals( 1, allRegistrations.filterIsInstance<CustomDeviceRegistration>().count() )
    }

    /**
     * Types which were wrapped in a 'Custom' type wrapper upon deserialization should be serialized to their original form (returning the original type, not the wrapper).
     */
    @ExperimentalSerializationApi
    @Test
    fun serializing_unknown_types_removes_the_wrapper()
    {
        val serialized: String = serializeDeploymentSnapshotIncludingUnknownRegistration()
        val snapshot: StudyDeploymentSnapshot = JSON.decodeFromString( serialized )

        val customSerialized = JSON.encodeToString( snapshot )
        assertEquals( serialized, customSerialized )
    }

    /**
     * Create a serialized deployment (snapshot) of a study protocol with exactly one primary device which is registered using an unknown device registration.
     */
    @ExperimentalSerializationApi
    private fun serializeDeploymentSnapshotIncludingUnknownRegistration(): String
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration( "Stub" )
        protocol.addPrimaryDevice( primary )
        val deployment = studyDeploymentFor( protocol )
        val registration = DefaultDeviceRegistration()
        deployment.registerDevice( primary, registration )

        var serialized: String = JSON.encodeToString( deployment.getSnapshot() )
        serialized = serialized.makeUnknown( registration )

        return serialized
    }
}
