package dk.cachet.carp.client.domain

import dk.cachet.carp.client.infrastructure.fromJson
import dk.cachet.carp.client.infrastructure.toJson
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.DeviceDeploymentStatus
import dk.cachet.carp.protocols.domain.devices.AltBeaconDeviceRegistration
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [StudyRuntime].
 */
class StudyRuntimeTest
{
    @Test
    fun initialize_matches_requested_runtime() = runBlockingTest {
        // Create a deployment service which contains a 'smartphone study'.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        val deviceRegistration = smartphone.createRegistration()
        val runtime = StudyRuntime.initialize( deploymentService, deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        assertEquals( deploymentStatus.studyDeploymentId, runtime.studyDeploymentId )
        assertEquals( smartphone.roleName, runtime.device.roleName )
    }

    @Test
    fun initialize_deploys_when_possible() = runBlockingTest {
        // Create a deployment service which contains a 'smartphone study'.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        val deviceRegistration = smartphone.createRegistration()
        val runtime = StudyRuntime.initialize( deploymentService, deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val updatedStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )

        assertTrue( runtime.isDeployed )
        assertEquals( 1, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.Deployed>().count() )
        assertTrue( updatedStatus.getDeviceStatus( smartphone ) is DeviceDeploymentStatus.Deployed )
    }

    @Test
    fun initialize_does_not_deploy_when_depending_on_other_devices() = runBlockingTest {
        // Create a deployment service which contains a study where 'smartphone' depends on another master device.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )

        val deviceRegistration = smartphone.createRegistration()
        val runtime = StudyRuntime.initialize( deploymentService, deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val updatedStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )

        assertFalse( runtime.isDeployed )
        assertEquals( 0, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.Deployed>().count() )
        assertTrue( updatedStatus.getDeviceStatus( smartphone ) is DeviceDeploymentStatus.NotDeployed )
    }

    @Test
    fun initialize_fails_for_unknown_studyDeploymentId() = runBlockingTest {
        // Create a deployment service which contains a 'smartphone study'.
        val ( deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )

        val unknownId = UUID.randomUUID()
        val deviceRegistration = smartphone.createRegistration()
        assertFailsWith<IllegalArgumentException>
            { StudyRuntime.initialize( deploymentService, unknownId, smartphone.roleName, deviceRegistration ) }
    }

    @Test
    fun initialize_fails_for_unknown_deviceRoleName() = runBlockingTest {
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        val deviceRegistration = smartphone.createRegistration()
        assertFailsWith<IllegalArgumentException>
            { StudyRuntime.initialize( deploymentService, deploymentStatus.studyDeploymentId, "Unknown role", deviceRegistration ) }
    }

    @Test
    fun initialize_fails_for_incorrect_deviceRegistration() = runBlockingTest {
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        val incorrectRegistration = AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0 )
        assertFailsWith<IllegalArgumentException>
            { StudyRuntime.initialize( deploymentService, deploymentStatus.studyDeploymentId, smartphone.roleName, incorrectRegistration ) }
    }

    @Test
    fun tryDeployment_only_succeeds_after_dependent_devices_are_registered() = runBlockingTest {
        // Create a study runtime for a study where 'smartphone' depends on another master device ('deviceSmartphoneDependsOn').
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val deviceRegistration = smartphone.createRegistration()
        val runtime = StudyRuntime.initialize( deploymentService, deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        // Dependent devices are not yet registered.
        var wasDeployed = runtime.tryDeployment( deploymentService )
        assertEquals( 0, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.Deployed>().count() )
        assertFalse( wasDeployed )

        // Once dependent devices are registered, deployment succeeds.
        deploymentService.registerDevice( deploymentStatus.studyDeploymentId, deviceSmartphoneDependsOn.roleName, deviceSmartphoneDependsOn.createRegistration() )
        wasDeployed = runtime.tryDeployment( deploymentService )
        assertEquals( 1, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.Deployed>().count() )
        assertTrue( wasDeployed )
    }

    @Test
    fun creating_runtime_fromSnapshot_obtained_by_getSnapshot_is_the_same() = runBlockingTest {
        // Create a study runtime snapshot for the 'smartphone' in 'smartphone study'.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val deviceRegistration = smartphone.createRegistration()
        val runtime = StudyRuntime.initialize( deploymentService, deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val snapshot = StudyRuntimeSnapshot.fromStudyRuntime( runtime )

        val serialized = snapshot.toJson()
        val parsed = StudyRuntimeSnapshot.fromJson( serialized )

        assertEquals( runtime.studyDeploymentId, parsed.studyDeploymentId )
        assertEquals( runtime.device, parsed.device )
        assertEquals( runtime.isDeployed, parsed.isDeployed )
        assertEquals( runtime.deploymentInformation, parsed.deploymentInformation )
    }
}
