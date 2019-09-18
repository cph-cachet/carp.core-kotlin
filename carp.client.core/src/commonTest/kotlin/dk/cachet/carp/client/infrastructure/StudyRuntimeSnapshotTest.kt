package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.domain.*
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistrationBuilder
import kotlin.test.*


/**
 * Tests for [StudyRuntimeSnapshot].
 */
class StudyRuntimeSnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON()
    {
        // Create deployment with one 'smartphone' study.
        val ( deploymentManager, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val deviceRegistration = DefaultDeviceRegistrationBuilder().build()
        val runtime = StudyRuntime.initialize( deploymentManager, deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val snapshot = StudyRuntimeSnapshot.fromStudyRuntime( runtime )

        val serialized = snapshot.toJson()
        val parsed = StudyRuntimeSnapshot.fromJson( serialized )
        assertEquals( snapshot, parsed )
    }
}