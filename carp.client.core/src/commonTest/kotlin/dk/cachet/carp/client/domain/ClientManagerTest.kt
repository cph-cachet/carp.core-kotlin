package dk.cachet.carp.client.domain

import dk.cachet.carp.client.infrastructure.InMemoryClientRepository
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


private val unknownId = UUID( "00000000-0000-0000-0000-000000000000" )

/**
 * Tests for [ClientManager].
 */
class ClientManagerTest
{
    private suspend fun initializeSmartphoneClient( deploymentService: DeploymentService ): SmartphoneClient =
        SmartphoneClient( InMemoryClientRepository(), deploymentService, createDataCollectorFactory() ).apply {
            configure()
        }


    @Test
    fun configure_succeeds() = runSuspendTest {
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )

        // Initially not configured.
        val client = SmartphoneClient( InMemoryClientRepository(), deploymentService, createDataCollectorFactory() )
        assertFalse( client.isConfigured() )

        // Configuration succeeds.
        client.configure()
        assertTrue( client.isConfigured() )
    }

    @Test
    fun add_study_fails_when_not_yet_configured() = runSuspendTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = SmartphoneClient( InMemoryClientRepository(), deploymentService, createDataCollectorFactory() )

        assertFailsWith<IllegalArgumentException>
        {
            client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        }
    }

    @Test
    fun add_study_succeeds() = runSuspendTest {
        // Create deployment service and client manager.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
    }

    @Test
    fun add_study_fails_for_invalid_deployment() = runSuspendTest {
        // Create deployment service and client manager.
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        assertFailsWith<IllegalArgumentException>
        {
            client.addStudy( unknownId, smartphone.roleName )
        }
    }

    @Test
    fun add_study_fails_for_nonexisting_device_role() = runSuspendTest {
        // Create deployment service and client manager.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        assertFailsWith<IllegalArgumentException>
        {
            client.addStudy( deploymentStatus.studyDeploymentId, "Invalid role" )
        }
    }

    @Test
    fun add_study_fails_for_study_which_was_already_added() = runSuspendTest {
        // Create deployment service and client manager.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        assertFailsWith<IllegalArgumentException>
        {
            client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        }
    }

    @Test
    fun tryDeployment_succeeds() = runSuspendTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        var status: StudyRuntimeStatus = client.addStudy( deploymentId, smartphone.roleName )

        // Dependent device needs to be registered before the intended device can be deployed on this client.
        assertTrue( status is StudyRuntimeStatus.NotReadyForDeployment )
        val dependentRegistration = deviceSmartphoneDependsOn.createRegistration()
        deploymentService.registerDevice( deploymentId, deviceSmartphoneDependsOn.roleName, dependentRegistration )

        status = client.tryDeployment( client.getStudiesStatus().first().id )
        assertTrue( status is StudyRuntimeStatus.Deployed )
    }

    @Test
    fun tryDeployment_succeeds_after_registering_devices() = runSuspendTest {
        val (deploymentService, deploymentStatus) =
            createStudyDeployment( createSmartphoneWithConnectedDeviceStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        var status: StudyRuntimeStatus = client.addStudy( deploymentId, smartphone.roleName )

        // Connected device needs to be registered before deployment can complete.
        // TODO: It should be possible to register this device through `ClientManager` rather than directly from `deploymentService`.
        assertTrue( status is StudyRuntimeStatus.RegisteringDevices )
        val connectedRegistration = connectedDevice.createRegistration()
        deploymentService.registerDevice( deploymentId, connectedDevice.roleName, connectedRegistration )

        status = client.tryDeployment( client.getStudiesStatus().first().id )
        assertTrue( status is StudyRuntimeStatus.Deployed )
    }

    @Test
    fun tryDeployment_returns_true_when_already_deployed() = runSuspendTest {
        // Add a study which instantly deploys given that the protocol only contains one master device.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        var status: StudyRuntimeStatus = client.addStudy( deploymentId, smartphone.roleName )
        assertTrue( status is StudyRuntimeStatus.Deployed )

        status = client.tryDeployment( client.getStudiesStatus().first().id )
        assertTrue( status is StudyRuntimeStatus.Deployed )
    }

    @Test
    fun tryDeployment_fails_for_unknown_id() = runSuspendTest {
        val (deploymentService, _) = createStudyDeployment( createDependentSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        assertFailsWith<IllegalArgumentException>
        {
            client.tryDeployment( StudyRuntimeId( unknownId, "Unknown device role" ) )
        }
    }
}
