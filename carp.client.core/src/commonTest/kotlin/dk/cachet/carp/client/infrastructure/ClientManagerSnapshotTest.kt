package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.domain.*
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [ClientManagerSnapshot].
 */
class ClientManagerSnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON() = runBlockingTest {
        // Create deployment service and client manager with one study.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = SmartphoneManager( smartphone.createRegistration(), deploymentService )
        clientManager.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val snapshot = clientManager.getSnapshot()

        val serialized = snapshot.toJson()
        val parsed = ClientManagerSnapshot.fromJson( serialized )
        assertEquals( snapshot, parsed )
    }
}
