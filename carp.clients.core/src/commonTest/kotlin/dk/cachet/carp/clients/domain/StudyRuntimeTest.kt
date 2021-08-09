package dk.cachet.carp.clients.domain

import dk.cachet.carp.clients.domain.data.AnyConnectedDeviceDataCollector
import dk.cachet.carp.clients.domain.data.DataListener
import dk.cachet.carp.clients.domain.data.DeviceDataCollectorFactory
import dk.cachet.carp.clients.domain.data.StubConnectedDeviceDataCollectorFactory
import dk.cachet.carp.clients.domain.data.StubDeviceDataCollector
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.AltBeaconDeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceType
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [StudyRuntime].
 */
class StudyRuntimeTest
{
    @Test
    fun initialize_matches_requested_runtime() = runSuspendTest {
        // Create a deployment service which contains a 'smartphone study'.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        // Initialize study runtime.
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        assertEquals( deploymentStatus.studyDeploymentId, runtime.studyDeploymentId )
        assertEquals( smartphone, runtime.device )
    }

    @Test
    fun initialize_deploys_when_possible() = runSuspendTest {
        // Create a deployment service which contains a 'smartphone study'.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        // Initialize study runtime.
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        // Study runtime status is deployed and contains registered master device.
        assertTrue( runtime.isDeployed )
        val runtimeStatus = runtime.getStatus()
        assertTrue( runtimeStatus is StudyRuntimeStatus.Deployed )
        val registrationStatus = runtimeStatus.devicesRegistrationStatus.values.singleOrNull()
        assertTrue( registrationStatus is DeviceRegistrationStatus.Registered )
        assertEquals( smartphone, registrationStatus.device )
        assertEquals( deviceRegistration, registrationStatus.registration )

        // Study runtime events reflects deployment has been received and completed.
        val events = runtime.consumeEvents()
        val receivedEvent = events.filterIsInstance<StudyRuntime.Event.DeploymentReceived>()
        assertEquals( 1, receivedEvent.count() )
        assertEquals( runtimeStatus.deploymentInformation, receivedEvent.single().deploymentInformation )
        assertEquals( 1, events.filterIsInstance<StudyRuntime.Event.DeploymentCompleted>().count() )

        // Master device status in deployment is also set to deployed.
        val newDeploymentStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )
        val masterDeviceStatus = newDeploymentStatus.getDeviceStatus( smartphone )
        assertTrue( masterDeviceStatus is DeviceDeploymentStatus.Deployed )
    }

    @Test
    fun initialize_does_not_deploy_when_depending_on_other_devices() = runSuspendTest {
        // Create a deployment service which contains a study where 'smartphone' depends on another master device.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )

        // Initialize study runtime.
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        // Study runtime status is not ready for deployment.
        assertFalse( runtime.isDeployed )
        val runtimeStatus = runtime.getStatus()
        assertTrue( runtimeStatus is StudyRuntimeStatus.NotReadyForDeployment )

        // Study runtime events reflects deployment has not been received yet.
        val events = runtime.consumeEvents()
        assertEquals( 0, events.filterIsInstance<StudyRuntime.Event.DeploymentReceived>().count() )

        // Master device status in deployment is registered, but not deployed.
        val newDeploymentStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )
        val masterDeviceStatus = newDeploymentStatus.getDeviceStatus( smartphone )
        assertTrue( masterDeviceStatus is DeviceDeploymentStatus.Registered )
    }

    @Test
    fun initialize_does_not_deploy_when_registering_devices() = runSuspendTest {
        // Create a deployment service which contains a study where 'smartphone' depends on a connected device.
        val (deploymentService, deploymentStatus) =
            createStudyDeployment( createSmartphoneWithConnectedDeviceStudy() )

        // Initialize study runtime.
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        // Study runtime status indicates `connectedDevice` needs to be registered.
        assertFalse( runtime.isDeployed )
        val runtimeStatus = runtime.getStatus()
        assertTrue( runtimeStatus is StudyRuntimeStatus.RegisteringDevices )
        assertEquals( connectedDevice, runtimeStatus.remainingDevicesToRegister.single() )
        val connectedRegistrationStatus = runtimeStatus.devicesRegistrationStatus[ connectedDevice ]
        assertTrue( connectedRegistrationStatus is DeviceRegistrationStatus.Unregistered )
        assertEquals( connectedDevice, connectedRegistrationStatus.device )

        // Study runtime events reflects deployment has been received, but not completed.
        val events = runtime.consumeEvents()
        val receivedEvent = events.filterIsInstance<StudyRuntime.Event.DeploymentReceived>()
        assertEquals( 1, receivedEvent.count() )
        assertEquals( runtimeStatus.deploymentInformation, receivedEvent.single().deploymentInformation )
        assertEquals( 0, events.filterIsInstance<StudyRuntime.Event.DeploymentCompleted>().count() )

        // Master device status in deployment is registered, but not deployed.
        val newDeploymentStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )
        val masterDeviceStatus = newDeploymentStatus.getDeviceStatus( smartphone )
        assertTrue( masterDeviceStatus is DeviceDeploymentStatus.Registered )
    }

    @Test
    fun initialize_fails_for_unknown_studyDeploymentId() = runSuspendTest {
        // Create a deployment service which contains a 'smartphone study'.
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )

        val unknownId = UUID.randomUUID()
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        assertFailsWith<IllegalArgumentException> {
            StudyRuntime.initialize(
                deploymentService, dataListener,
                unknownId, smartphone.roleName, deviceRegistration )
        }
    }

    @Test
    fun initialize_fails_for_unknown_deviceRoleName() = runSuspendTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        assertFailsWith<IllegalArgumentException> {
            StudyRuntime.initialize(
                deploymentService, dataListener,
                deploymentStatus.studyDeploymentId, "Unknown role", deviceRegistration )
        }
    }

    @Test
    fun initialize_fails_for_incorrect_deviceRegistration() = runSuspendTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        val incorrectRegistration = AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0, 0 )
        val dataListener = createDataListener()
        assertFailsWith<IllegalArgumentException> {
            StudyRuntime.initialize(
                deploymentService, dataListener,
                deploymentStatus.studyDeploymentId, smartphone.roleName, incorrectRegistration )
        }
    }

    @Test
    fun tryDeployment_only_succeeds_after_ready_for_deployment() = runSuspendTest {
        // Create a study runtime for a study where 'smartphone' depends on another master device ('deviceSmartphoneDependsOn').
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        // Dependent devices are not yet registered.
        var status = runtime.tryDeployment( deploymentService, dataListener )
        assertEquals( 0, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.DeploymentCompleted>().count() )
        assertTrue( status is StudyRuntimeStatus.NotReadyForDeployment )

        // Once dependent devices are registered, deployment succeeds.
        deploymentService.registerDevice(
            deploymentStatus.studyDeploymentId,
            deviceSmartphoneDependsOn.roleName,
            deviceSmartphoneDependsOn.createRegistration() )
        status = runtime.tryDeployment( deploymentService, dataListener )
        assertEquals( 1, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.DeploymentCompleted>().count() )
        assertTrue( status is StudyRuntimeStatus.Deployed )
        val registrationStatus = status.devicesRegistrationStatus.values.singleOrNull()
        assertTrue( registrationStatus is DeviceRegistrationStatus.Registered )
        assertEquals( smartphone, registrationStatus.device )
        assertEquals( deviceRegistration, registrationStatus.registration )
    }

    @Test
    fun tryDeployment_only_succeeds_after_devices_are_registered() = runSuspendTest {
        // Create a study runtime for a study where 'smartphone' depends on a connected device.
        val (deploymentService, deploymentStatus) =
            createStudyDeployment( createSmartphoneWithConnectedDeviceStudy() )
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        // Connected device is not yet registered.
        var status = runtime.tryDeployment( deploymentService, dataListener )
        assertEquals( 0, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.DeploymentCompleted>().count() )
        assertTrue( status is StudyRuntimeStatus.RegisteringDevices )

        // Once device is registered, deployment succeeds.
        // TODO: It should be possible to register this device through `StudyRuntime` rather than directly from `deploymentService`.
        deploymentService.registerDevice(
            deploymentStatus.studyDeploymentId,
            connectedDevice.roleName,
            connectedDevice.createRegistration() )
        status = runtime.tryDeployment( deploymentService, dataListener )
        assertEquals( 1, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.DeploymentCompleted>().count() )
        assertTrue( status is StudyRuntimeStatus.Deployed )
        val registrationStatuses = status.devicesRegistrationStatus
        assertEquals( 2, registrationStatuses.size ) // Smartphone and connected device.
        assertTrue( registrationStatuses[ smartphone ] is DeviceRegistrationStatus.Registered )
        assertTrue( registrationStatuses[ connectedDevice ] is DeviceRegistrationStatus.Registered )
    }

    @Test
    fun tryDeployment_returns_true_when_already_deployed() = runSuspendTest {
        // Create a study runtime which instantly deploys because the protocol only contains one master device.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        assertTrue( runtime.isDeployed )

        val status = runtime.tryDeployment( deploymentService, dataListener )
        assertTrue( status is StudyRuntimeStatus.Deployed )
    }

    @Test
    fun tryDeployment_succeeds_when_data_types_of_protocol_measures_are_supported() = runSuspendTest {
        // Create protocol that measures on smartphone and one connected device.
        val protocol = createSmartphoneWithConnectedDeviceStudy()
        val masterTask = StubTaskDescriptor( "Master measure", listOf( Measure.DataStream( STUB_DATA_TYPE ) ) )
        protocol.addTaskControl( smartphone.atStartOfStudy().start( masterTask, smartphone ) )
        val connectedDataType = DataType( "custom", "type" )
        val connectedTask = StubTaskDescriptor( "Connected measure", listOf( Measure.DataStream( connectedDataType ) ) )
        protocol.addTaskControl( smartphone.atStartOfStudy().start( connectedTask, connectedDevice ) )

        // Create a data listener which supports the requested devices and types in the protocol
        val dataListener = DataListener( StubConnectedDeviceDataCollectorFactory(
            localSupportedDataTypes = setOf( STUB_DATA_TYPE ),
            mapOf( StubDeviceDescriptor::class to setOf( connectedDataType ) )
        ) )

        // Create study deployment with preregistered connected device (otherwise study runtime initialization won't complete).
        val (deploymentService, deploymentStatus) = createStudyDeployment( protocol )
        deploymentService.registerDevice(
            deploymentStatus.studyDeploymentId,
            connectedDevice.roleName,
            connectedDevice.createRegistration() )

        // Initializing study runtime for the smartphone deployment should succeed since devices and data types are supported.
        val deviceRegistration = smartphone.createRegistration()
        val runtime = StudyRuntime.initialize( // This will 'tryDeployment'.
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        assertTrue( runtime.isDeployed )
    }

    @Test
    fun tryDeployment_fails_when_requested_data_cannot_be_collected() = runSuspendTest {
        // Create a protocol that has one measure.
        val protocol = createSmartphoneStudy()
        val task = StubTaskDescriptor( "One measure", listOf( Measure.DataStream( STUB_DATA_TYPE ) ) )
        protocol.addTaskControl( smartphone.atStartOfStudy().start( task, smartphone ) )

        // Initializing study runtime for the smartphone deployment should fail since StubMeasure can't be collected.
        val (deploymentService, deploymentStatus) = createStudyDeployment( protocol )
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener( supportedDataTypes = emptyArray() )
        assertFailsWith<UnsupportedOperationException>
        {
            StudyRuntime.initialize( // This will 'tryDeployment'.
                deploymentService, dataListener,
                deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        }
    }

    @Test
    fun tryDeployment_fails_when_connected_device_is_not_supported() = runSuspendTest {
        // Create a deployment for a protocol with a preregistered connected device but no measures.
        val (deploymentService, deploymentStatus) =
            createStudyDeployment( createSmartphoneWithConnectedDeviceStudy() )
        deploymentService.registerDevice(
            deploymentStatus.studyDeploymentId,
            connectedDevice.roleName,
            connectedDevice.createRegistration() )
        val deviceRegistration = smartphone.createRegistration()

        // Create a listener which does not support measuring on the connected device.
        val localDataCollector = StubDeviceDataCollector( emptySet() )
        val factory =
            object : DeviceDataCollectorFactory( localDataCollector )
            {
                override fun createConnectedDataCollector(
                    deviceType: DeviceType,
                    deviceRegistration: DeviceRegistration
                ): AnyConnectedDeviceDataCollector = throw UnsupportedOperationException( "Unsupported device type." )
            }
        val dataListener = DataListener( factory )

        // Even though there are no measures for the connected device in the protocol, it should still verify support.
        assertFailsWith<UnsupportedOperationException>
        {
            StudyRuntime.initialize( // This will 'tryDeployment'.
                deploymentService, dataListener,
                deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        }
    }

    @Test
    fun stop_succeeds() = runSuspendTest {
        // Initialize a study runtime for a typical 'smartphone study'.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        check( runtime.isDeployed )
        runtime.consumeEvents() // Drop events so only new ones under test appear.

        val status = runtime.stop( deploymentService )

        // Study runtime status reflects the study has stopped.
        assertTrue( runtime.isStopped )
        assertTrue( status is StudyRuntimeStatus.Stopped )
        assertTrue( runtime.isDeployed ) // The device is still considered deployed.

        // Study runtime events reflects deployment has stopped
        assertEquals( 1, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.DeploymentStopped>().count() )

        // Deployment status also reflects deployment has stopped.
        val newDeploymentStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )
        assertTrue( newDeploymentStatus is StudyDeploymentStatus.Stopped )
    }

    @Test
    fun creating_runtime_fromSnapshot_obtained_by_getSnapshot_is_the_same() = runSuspendTest {
        // Create a study runtime snapshot for the 'smartphone' with an unregistered connected device.
        val protocol = createSmartphoneWithConnectedDeviceStudy()
        val (deploymentService, deploymentStatus) = createStudyDeployment( protocol )
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val snapshot = runtime.getSnapshot()
        val fromSnapshot = StudyRuntime.fromSnapshot( snapshot )

        assertEquals( runtime.studyDeploymentId, fromSnapshot.studyDeploymentId )
        assertEquals( runtime.createdOn, fromSnapshot.createdOn )
        assertEquals( runtime.device, fromSnapshot.device )
        assertEquals( runtime.isDeployed, fromSnapshot.isDeployed )
        assertEquals( runtime.isStopped, fromSnapshot.isStopped )
        assertEquals( runtime.getStatus(), fromSnapshot.getStatus() )
        assertEquals( 0, fromSnapshot.consumeEvents().size )
    }
}
