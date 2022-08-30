package dk.cachet.carp.clients.application

import dk.cachet.carp.clients.application.study.StudyStatus
import dk.cachet.carp.clients.connectedDevice
import dk.cachet.carp.clients.createDataCollectorFactory
import dk.cachet.carp.clients.createDependentSmartphoneStudy
import dk.cachet.carp.clients.createSmartphoneStudy
import dk.cachet.carp.clients.createSmartphoneWithConnectedDeviceStudy
import dk.cachet.carp.clients.createStudyDeployment
import dk.cachet.carp.clients.deviceSmartphoneDependsOn
import dk.cachet.carp.clients.domain.DeviceRegistrationStatus
import dk.cachet.carp.clients.domain.SmartphoneClient
import dk.cachet.carp.clients.infrastructure.InMemoryClientRepository
import dk.cachet.carp.clients.smartphone
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.DeploymentService
import kotlinx.coroutines.test.runTest
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
    fun configure_succeeds() = runTest {
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )

        // Initially not configured.
        val client = SmartphoneClient( InMemoryClientRepository(), deploymentService, createDataCollectorFactory() )
        assertFalse( client.isConfigured() )

        // Configuration succeeds.
        client.configure()
        assertTrue( client.isConfigured() )
    }

    @Test
    fun add_study_succeeds() = runTest {
        // Create deployment service and client manager.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        val status = client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        assertEquals( status, client.getStudyStatusList().singleOrNull() )
    }

    @Test
    fun add_study_fails_for_study_which_was_already_added() = runTest {
        // Create deployment service and client manager.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        assertFailsWith<IllegalArgumentException>
        {
            val status = client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
            client.tryDeployment( status.id )
        }
    }

    @Test
    fun tryDeployment_succeeds() = runTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        var status: StudyStatus = client.addStudy( deploymentId, smartphone.roleName )
        status = client.tryDeployment( status.id )

        // Dependent device needs to be registered before the intended device can be deployed on this client.
        assertTrue( status is StudyStatus.AwaitingOtherDeviceRegistrations )
        val dependentRegistration = deviceSmartphoneDependsOn.createRegistration()
        deploymentService.registerDevice( deploymentId, deviceSmartphoneDependsOn.roleName, dependentRegistration )

        status = client.tryDeployment( status.id )
        assertTrue( status is StudyStatus.AwaitingOtherDeviceDeployments )
    }

    @Test
    fun tryDeployment_succeeds_after_registering_devices() = runTest {
        val (deploymentService, deploymentStatus) =
            createStudyDeployment( createSmartphoneWithConnectedDeviceStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        var status: StudyStatus = client.addStudy( deploymentId, smartphone.roleName )
        status = client.tryDeployment( status.id )

        // Connected device needs to be registered before deployment can complete.
        // TODO: It should be possible to register this device through `ClientManager` rather than directly from `deploymentService`.
        assertTrue( status is StudyStatus.RegisteringDevices )
        val connectedRegistration = connectedDevice.createRegistration()
        deploymentService.registerDevice( deploymentId, connectedDevice.roleName, connectedRegistration )

        status = client.tryDeployment( status.id )
        assertTrue( status is StudyStatus.Running )
    }

    @Test
    fun tryDeployment_succeeds_when_already_deployed() = runTest {
        // Add a study which instantly deploys given that the protocol only contains one primary device.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        var status: StudyStatus = client.addStudy( deploymentId, smartphone.roleName )
        status = client.tryDeployment( status.id )
        assertTrue( status is StudyStatus.Running )

        status = client.tryDeployment( status.id )
        assertTrue( status is StudyStatus.Running )
    }

    @Test
    fun tryDeployment_fails_when_not_yet_configured() = runTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = SmartphoneClient( InMemoryClientRepository(), deploymentService, createDataCollectorFactory() )
        val status = client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )

        assertFailsWith<IllegalArgumentException> { client.tryDeployment( status.id ) }
    }

    @Test
    fun tryDeployment_fails_for_unknown_study_id() = runTest {
        val (deploymentService, _) = createStudyDeployment( createDependentSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        assertFailsWith<IllegalArgumentException> { client.tryDeployment( unknownId ) }
    }

    @Test
    fun tryDeployment_fails_for_invalid_deployment() = runTest {
        // Create deployment service and client manager.
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val status = client.addStudy( unknownId, smartphone.roleName )

        assertFailsWith<IllegalArgumentException> { client.tryDeployment( status.id ) }
    }

    @Test
    fun tryDeployment_fails_for_nonexisting_device_role() = runTest {
        // Create deployment service and client manager.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val status = client.addStudy( deploymentStatus.studyDeploymentId, "Invalid role" )

        assertFailsWith<IllegalArgumentException> { client.tryDeployment( status.id ) }
    }

    @Test
    fun stopStudy_succeeds() = runTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val status: StudyStatus = client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )

        val newStatus = client.stopStudy( status.id )
        assertTrue( newStatus is StudyStatus.Stopped )
    }

    @Test
    fun stopStudy_fails_for_unknown_id() = runTest {
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        assertFailsWith<IllegalArgumentException> { client.stopStudy( unknownId ) }
    }

    @Test
    fun getStudyStatusList_returns_latest_status() = runTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        var status: StudyStatus = client.addStudy( deploymentId, smartphone.roleName )

        // Register dependent device and deploy client.
        val dependentRegistration = deviceSmartphoneDependsOn.createRegistration()
        deploymentService.registerDevice( deploymentId, deviceSmartphoneDependsOn.roleName, dependentRegistration )
        status = client.tryDeployment( status.id )
        check( status is StudyStatus.AwaitingOtherDeviceDeployments )
        assertEquals( status, client.getStudyStatusList().first() )

        // Stop client.
        status = client.stopStudy( status.id )
        assertEquals( status, client.getStudyStatusList().first() )
    }

    @Test
    fun getConnectedDeviceManager_succeeds() = runTest {
        // Create study deployment.
        val (deploymentService, deploymentStatus) =
            createStudyDeployment( createSmartphoneWithConnectedDeviceStudy() )
        val deploymentId = deploymentStatus.studyDeploymentId

        // Preregister the connected device so that registration is instantly available.
        val connectedRegistration = connectedDevice.createRegistration()
        deploymentService.registerDevice( deploymentId, connectedDevice.roleName, connectedRegistration )

        // Get device registration status.
        val client = initializeSmartphoneClient( deploymentService )
        var studyStatus: StudyStatus = client.addStudy( deploymentId, smartphone.roleName )
        studyStatus = client.tryDeployment( studyStatus.id )
        assertTrue( studyStatus is StudyStatus.DeviceDeploymentReceived )
        val deviceStatus = studyStatus.devicesRegistrationStatus[ connectedDevice ]
        assertTrue( deviceStatus is DeviceRegistrationStatus.Registered )

        val deviceManager = client.getConnectedDeviceManager( deviceStatus )
        assertEquals( connectedRegistration, deviceManager.deviceRegistration )
    }
}
