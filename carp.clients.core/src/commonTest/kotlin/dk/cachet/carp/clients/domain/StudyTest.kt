package dk.cachet.carp.clients.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [Study].
 */
class StudyTest
{
    @Test
    fun creating_study_fromSnapshot_obtained_by_getSnapshot_is_the_same() = runSuspendTest {
        // Create a study snapshot for the 'smartphone' with an unregistered connected device.
        val study = Study( UUID.randomUUID(), smartphone.roleName )
        val connectedDevices = setOf( connectedDevice )
        val masterDeviceDeployment = MasterDeviceDeployment(
            smartphone,
            smartphone.createRegistration(),
            connectedDevices
        )
        study.deploymentReceived( masterDeviceDeployment, connectedDevices )
        val snapshot = study.getSnapshot()
        val fromSnapshot = Study.fromSnapshot( snapshot )

        assertEquals( study.studyDeploymentId, fromSnapshot.studyDeploymentId )
        assertEquals( study.createdOn, fromSnapshot.createdOn )
        assertEquals( study.deviceRoleName, fromSnapshot.deviceRoleName )
        assertEquals( study.isDeployed, fromSnapshot.isDeployed )
        assertEquals( study.isStopped, fromSnapshot.isStopped )
        assertEquals( study.getStatus(), fromSnapshot.getStatus() )
        assertEquals( 0, fromSnapshot.consumeEvents().size )
    }
}
