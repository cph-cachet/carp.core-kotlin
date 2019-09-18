package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.application.*
import dk.cachet.carp.client.domain.*
import kotlin.test.*


/**
 * Tests for [ClientManagerSnapshot].
 */
class ClientManagerSnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON()
    {
        // Create deployment and client manager with one study.
        val ( deploymentManager, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = SmartphoneManager( smartphone.createRegistration(), deploymentManager )
        clientManager.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val snapshot = ClientManagerSnapshot.fromClientManager( clientManager )

        val serialized = snapshot.toJson()
        val parsed = ClientManagerSnapshot.fromJson( serialized )
        assertEquals( snapshot, parsed )
    }
}