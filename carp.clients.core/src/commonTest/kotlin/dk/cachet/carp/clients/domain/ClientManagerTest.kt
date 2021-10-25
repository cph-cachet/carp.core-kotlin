package dk.cachet.carp.clients.domain

import dk.cachet.carp.clients.infrastructure.InMemoryClientRepository
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.DeploymentService
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
        var status: StudyStatus = client.addStudy( deploymentId, smartphone.roleName )

        // Dependent device needs to be registered before the intended device can be deployed on this client.
        assertTrue( status is StudyStatus.AwaitingOtherDeviceRegistrations )
        val dependentRegistration = deviceSmartphoneDependsOn.createRegistration()
        deploymentService.registerDevice( deploymentId, deviceSmartphoneDependsOn.roleName, dependentRegistration )

        status = client.tryDeployment( status.id )
        assertTrue( status is StudyStatus.AwaitingOtherDeviceDeployments )
    }

    @Test
    fun tryDeployment_succeeds_after_registering_devices() = runSuspendTest {
        val (deploymentService, deploymentStatus) =
            createStudyDeployment( createSmartphoneWithConnectedDeviceStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        var status: StudyStatus = client.addStudy( deploymentId, smartphone.roleName )

        // Connected device needs to be registered before deployment can complete.
        // TODO: It should be possible to register this device through `ClientManager` rather than directly from `deploymentService`.
        assertTrue( status is StudyStatus.RegisteringDevices )
        val connectedRegistration = connectedDevice.createRegistration()
        deploymentService.registerDevice( deploymentId, connectedDevice.roleName, connectedRegistration )

        status = client.tryDeployment( status.id )
        assertTrue( status is StudyStatus.Running )
    }

    @Test
    fun tryDeployment_succeeds_when_already_deployed() = runSuspendTest {
        // Add a study which instantly deploys given that the protocol only contains one master device.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        var status: StudyStatus = client.addStudy( deploymentId, smartphone.roleName )
        assertTrue( status is StudyStatus.Running )

        status = client.tryDeployment( status.id )
        assertTrue( status is StudyStatus.Running )
    }

    @Test
    fun tryDeployment_fails_for_unknown_id() = runSuspendTest {
        val (deploymentService, _) = createStudyDeployment( createDependentSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        assertFailsWith<IllegalArgumentException>
        {
            client.tryDeployment( StudyId( unknownId, "Unknown device role" ) )
        }
    }

    @Test
    fun stopStudy_succeeds() = runSuspendTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val status: StudyStatus = client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )

        val newStatus = client.stopStudy( status.id )
        assertTrue( newStatus is StudyStatus.Stopped )
    }

    @Test
    fun stopStudy_fails_for_unknown_id() = runSuspendTest {
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        assertFailsWith<IllegalArgumentException>
        {
            client.stopStudy( StudyId( unknownId, "Unknown device role" ) )
        }
    }

    @Test
    fun getStudiesStatus_returns_latest_status() = runSuspendTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        var status: StudyStatus = client.addStudy( deploymentId, smartphone.roleName )

        // Register dependent device and deploy client.
        check( status is StudyStatus.AwaitingOtherDeviceRegistrations )
        val dependentRegistration = deviceSmartphoneDependsOn.createRegistration()
        deploymentService.registerDevice( deploymentId, deviceSmartphoneDependsOn.roleName, dependentRegistration )
        status = client.tryDeployment( status.id )
        check( status is StudyStatus.AwaitingOtherDeviceDeployments )
        assertEquals( status, client.getStudiesStatus().first() )

        // Stop client.
        status = client.stopStudy( status.id )
        assertEquals( status, client.getStudiesStatus().first() )
    }

    @Test
    fun getConnectedDeviceManager_succeeds() = runSuspendTest {
        // Create study deployment.
        val (deploymentService, deploymentStatus) =
            createStudyDeployment( createSmartphoneWithConnectedDeviceStudy() )
        val deploymentId = deploymentStatus.studyDeploymentId

        // Preregister the connected device so that registration is instantly available.
        val connectedRegistration = connectedDevice.createRegistration()
        deploymentService.registerDevice( deploymentId, connectedDevice.roleName, connectedRegistration )

        // Get device registration status.
        val client = initializeSmartphoneClient( deploymentService )
        val studyStatus: StudyStatus = client.addStudy( deploymentId, smartphone.roleName )
        assertTrue( studyStatus is StudyStatus.DeviceDeploymentReceived )
        val deviceStatus = studyStatus.devicesRegistrationStatus[ connectedDevice ]
        assertTrue( deviceStatus is DeviceRegistrationStatus.Registered )

        val deviceManager = client.getConnectedDeviceManager( deviceStatus )
        assertEquals( connectedRegistration, deviceManager.deviceRegistration )
    }
}
