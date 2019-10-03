package dk.cachet.carp.client.domain

import dk.cachet.carp.client.infrastructure.*
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

        assertTrue( runtime.isDeployed )
    }

    @Test
    fun initialize_does_not_deploy_when_depending_on_other_devices() = runBlockingTest {
        // Create a deployment service which contains a study where 'smartphone' depends on another master device.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )

        val deviceRegistration = smartphone.createRegistration()
        val runtime = StudyRuntime.initialize( deploymentService, deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        assertFalse( runtime.isDeployed )
    }

    @Test
    fun tryDeployment_only_succeeds_after_dependent_devices_are_registered() = runBlockingTest {
        // Create a study runtime for a study where 'smartphone' depends on another master device ('deviceSmartphoneDependsOn').
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val deviceRegistration = smartphone.createRegistration()
        val runtime = StudyRuntime.initialize( deploymentService, deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        // Dependent devices are not yet registered.
        val status = runtime.tryDeployment()
        assertFalse( status.isReadyForDeployment )
        assertFalse( status.isDeployed )

        // Once dependent devices are registered, deployment succeeds.
        deploymentService.registerDevice( deploymentStatus.studyDeploymentId, deviceSmartphoneDependsOn.roleName, deviceSmartphoneDependsOn.createRegistration() )
        val succeededStatus = runtime.tryDeployment()
        assertTrue( succeededStatus.isReadyForDeployment )
        assertTrue( succeededStatus.isDeployed )
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