package dk.cachet.carp.clients.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [StudyRuntime].
 */
class StudyRuntimeTest
{
    @Test
    fun creating_runtime_fromSnapshot_obtained_by_getSnapshot_is_the_same() = runSuspendTest {
        // Create a study runtime snapshot for the 'smartphone' with an unregistered connected device.
        val runtime = StudyRuntime( UUID.randomUUID(), smartphone.roleName )
        val connectedDevices = setOf( connectedDevice )
        val masterDeviceDeployment = MasterDeviceDeployment(
            smartphone,
            smartphone.createRegistration(),
            connectedDevices
        )
        runtime.deploymentReceived( masterDeviceDeployment, connectedDevices )
        val snapshot = runtime.getSnapshot()
        val fromSnapshot = StudyRuntime.fromSnapshot( snapshot )

        assertEquals( runtime.studyDeploymentId, fromSnapshot.studyDeploymentId )
        assertEquals( runtime.createdOn, fromSnapshot.createdOn )
        assertEquals( runtime.deviceRoleName, fromSnapshot.deviceRoleName )
        assertEquals( runtime.isDeployed, fromSnapshot.isDeployed )
        assertEquals( runtime.isStopped, fromSnapshot.isStopped )
        assertEquals( runtime.getStatus(), fromSnapshot.getStatus() )
        assertEquals( 0, fromSnapshot.consumeEvents().size )
    }
}
