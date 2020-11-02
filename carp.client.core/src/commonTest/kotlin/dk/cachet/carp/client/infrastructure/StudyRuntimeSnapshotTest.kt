package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.domain.createSmartphoneStudy
import dk.cachet.carp.client.domain.createStudyDeployment
import dk.cachet.carp.client.domain.smartphone
import dk.cachet.carp.client.domain.StudyRuntime
import dk.cachet.carp.client.domain.StudyRuntimeSnapshot
import dk.cachet.carp.client.domain.createDataListener
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistrationBuilder
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [StudyRuntimeSnapshot].
 */
class StudyRuntimeSnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON() = runSuspendTest {
        // Create deployment service with one 'smartphone' study.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val deviceRegistration = DefaultDeviceRegistrationBuilder().build()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val snapshot = StudyRuntimeSnapshot.fromStudyRuntime( runtime )

        val serialized = snapshot.toJson()
        val parsed = StudyRuntimeSnapshot.fromJson( serialized )
        assertEquals( snapshot, parsed )
    }
}
