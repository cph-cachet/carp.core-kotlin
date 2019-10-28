package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.domain.*
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistrationBuilder
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [StudyRuntimeSnapshot].
 */
class StudyRuntimeSnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON() = runBlockingTest {
        // Create deployment service with one 'smartphone' study.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val deviceRegistration = DefaultDeviceRegistrationBuilder().build()
        val runtime = StudyRuntime.initialize( deploymentService, deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val snapshot = StudyRuntimeSnapshot.fromStudyRuntime( runtime )

        val serialized = snapshot.toJson()
        val parsed = StudyRuntimeSnapshot.fromJson( serialized )
        assertEquals( snapshot, parsed )
    }
}