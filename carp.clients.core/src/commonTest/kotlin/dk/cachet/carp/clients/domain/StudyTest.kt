package dk.cachet.carp.clients.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.test.runSuspendTest
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [Study].
 */
class StudyTest
{
    @Test
    fun creating_study_fromSnapshot_obtained_by_getSnapshot_is_the_same() = runSuspendTest {
        // Create a study snapshot for the 'smartphone' with an unregistered connected device.
        val deploymentId = UUID.randomUUID()
        val study = Study( deploymentId, smartphone.roleName )
        val connectedDevices = setOf( connectedDevice )
        val masterDeviceDeployment = MasterDeviceDeployment(
            smartphone,
            smartphone.createRegistration(),
            connectedDevices
        )
        study.deploymentStatusReceived(
            StudyDeploymentStatus.DeployingDevices(
                Clock.System.now(),
                deploymentId,
                listOf(
                    DeviceDeploymentStatus.Registered(
                        smartphone,
                        true,
                        emptySet(),
                        connectedDevices.map { it.roleName }.toSet()
                    )
                ),
                null
            )
        )
        study.deviceDeploymentReceived( masterDeviceDeployment )
        val snapshot = study.getSnapshot()
        val fromSnapshot = Study.fromSnapshot( snapshot )

        assertEquals( study.studyDeploymentId, fromSnapshot.studyDeploymentId )
        assertEquals( study.createdOn, fromSnapshot.createdOn )
        assertEquals( study.deviceRoleName, fromSnapshot.deviceRoleName )
        assertEquals( study.getStatus(), fromSnapshot.getStatus() )
        assertEquals( 0, fromSnapshot.consumeEvents().size )
    }
}
