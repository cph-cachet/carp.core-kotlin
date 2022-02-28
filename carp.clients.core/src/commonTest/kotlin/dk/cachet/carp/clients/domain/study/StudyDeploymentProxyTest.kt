package dk.cachet.carp.clients.domain.study

import dk.cachet.carp.clients.application.study.StudyStatus
import dk.cachet.carp.clients.domain.connectedDevice
import dk.cachet.carp.clients.domain.createDataListener
import dk.cachet.carp.clients.domain.createDependentSmartphoneStudy
import dk.cachet.carp.clients.domain.createSmartphoneStudy
import dk.cachet.carp.clients.domain.createSmartphoneWithConnectedDeviceStudy
import dk.cachet.carp.clients.domain.createStudyDeployment
import dk.cachet.carp.clients.domain.deviceSmartphoneDependsOn
import dk.cachet.carp.clients.domain.smartphone
import dk.cachet.carp.clients.domain.DeviceRegistrationStatus
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
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [StudyDeploymentProxy].
 */
class StudyDeploymentProxyTest
{
    @Test
    fun tryDeployment_deploys_when_possible() = runSuspendTest {
        // Create a deployment service which contains a 'smartphone study'.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val studyDeployment = StudyDeploymentProxy( deploymentService, createDataListener() )

        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val deviceRegistration = smartphone.createRegistration()
        studyDeployment.tryDeployment( study, deviceRegistration )

        // Study status is running and contains registered primary device.
        val studyStatus = study.getStatus()
        assertTrue( studyStatus is StudyStatus.Running )
        val registrationStatus = studyStatus.devicesRegistrationStatus.values.singleOrNull()
        assertTrue( registrationStatus is DeviceRegistrationStatus.Registered )
        assertEquals( smartphone, registrationStatus.device )
        assertEquals( deviceRegistration, registrationStatus.registration )

        // Primary device status in deployment is also set to deployed.
        val newDeploymentStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )
        val primaryDeviceStatus = newDeploymentStatus.getDeviceStatus( smartphone )
        assertTrue( primaryDeviceStatus is DeviceDeploymentStatus.Deployed )
    }

    @Test
    fun tryDeployment_does_not_deploy_when_depending_on_other_devices() = runSuspendTest {
        // Create a deployment service which contains a study where 'smartphone' depends on another primary device.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val studyDeployment = StudyDeploymentProxy( deploymentService, createDataListener() )

        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val deviceRegistration = smartphone.createRegistration()
        studyDeployment.tryDeployment( study, deviceRegistration )

        // Study status is not ready for deployment.
        val studyStatus = study.getStatus()
        assertTrue( studyStatus is StudyStatus.AwaitingOtherDeviceRegistrations )

        // Primary device status in deployment is registered, but not deployed.
        val newDeploymentStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )
        val primaryDeviceStatus = newDeploymentStatus.getDeviceStatus( smartphone )
        assertTrue( primaryDeviceStatus is DeviceDeploymentStatus.Registered )
    }

    @Test
    fun tryDeployment_does_not_deploy_when_registering_devices() = runSuspendTest {
        // Create a deployment service which contains a study where 'smartphone' depends on a connected device.
        val (deploymentService, deploymentStatus) =
            createStudyDeployment( createSmartphoneWithConnectedDeviceStudy() )
        val studyDeployment = StudyDeploymentProxy( deploymentService, createDataListener() )

        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val deviceRegistration = smartphone.createRegistration()
        studyDeployment.tryDeployment( study, deviceRegistration )

        // Study status indicates `connectedDevice` needs to be registered.
        val studyStatus = study.getStatus()
        assertTrue( studyStatus is StudyStatus.RegisteringDevices )
        assertEquals( connectedDevice, studyStatus.remainingDevicesToRegister.single() )
        val connectedRegistrationStatus = studyStatus.devicesRegistrationStatus[ connectedDevice ]
        assertTrue( connectedRegistrationStatus is DeviceRegistrationStatus.Unregistered )
        assertEquals( connectedDevice, connectedRegistrationStatus.device )

        // Primary device status in deployment is registered, but not deployed.
        val newDeploymentStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )
        val primaryDeviceStatus = newDeploymentStatus.getDeviceStatus( smartphone )
        assertTrue( primaryDeviceStatus is DeviceDeploymentStatus.Registered )
    }

    @Test
    fun tryDeployment_fails_for_unknown_studyDeploymentId() = runSuspendTest {
        // Create a deployment service which contains a 'smartphone study'.
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )
        val studyDeployment = StudyDeploymentProxy( deploymentService, createDataListener() )

        val unknownId = UUID.randomUUID()
        val study = Study( unknownId, smartphone.roleName )
        val deviceRegistration = smartphone.createRegistration()
        assertFailsWith<IllegalArgumentException> {
            studyDeployment.tryDeployment( study, deviceRegistration )
        }
    }

    @Test
    fun tryDeployment_fails_for_unknown_deviceRoleName() = runSuspendTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val studyDeployment = StudyDeploymentProxy( deploymentService, createDataListener() )

        val study = Study( deploymentStatus.studyDeploymentId, "Unknown role" )
        val deviceRegistration = smartphone.createRegistration()
        assertFailsWith<IllegalArgumentException> {
            studyDeployment.tryDeployment( study, deviceRegistration )
        }
    }

    @Test
    fun tryDeployment_fails_for_incorrect_deviceRegistration() = runSuspendTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val studyDeployment = StudyDeploymentProxy( deploymentService, createDataListener() )

        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val incorrectRegistration = AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0, 0 )
        assertFailsWith<IllegalArgumentException> {
            studyDeployment.tryDeployment( study, incorrectRegistration )
        }
    }

    @Test
    fun tryDeployment_only_succeeds_after_ready_for_deployment() = runSuspendTest {
        // Create a study where 'smartphone' depends on another primary device ('deviceSmartphoneDependsOn').
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val studyDeployment = StudyDeploymentProxy( deploymentService, createDataListener() )
        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val deviceRegistration = smartphone.createRegistration()
        studyDeployment.tryDeployment( study, deviceRegistration )

        // Dependent devices are not yet registered.
        var status = study.getStatus()
        assertTrue( status is StudyStatus.AwaitingOtherDeviceRegistrations )

        // Once dependent devices are registered, deployment succeeds.
        deploymentService.registerDevice(
            deploymentStatus.studyDeploymentId,
            deviceSmartphoneDependsOn.roleName,
            deviceSmartphoneDependsOn.createRegistration() )
        studyDeployment.tryDeployment( study, deviceRegistration )
        status = study.getStatus()
        assertTrue( status is StudyStatus.AwaitingOtherDeviceDeployments )
        val registrationStatus = status.devicesRegistrationStatus.values.singleOrNull()
        assertTrue( registrationStatus is DeviceRegistrationStatus.Registered )
        assertEquals( smartphone, registrationStatus.device )
        assertEquals( deviceRegistration, registrationStatus.registration )
    }

    @Test
    fun tryDeployment_only_succeeds_after_devices_are_registered() = runSuspendTest {
        // Create a study for a study where 'smartphone' depends on a connected device.
        val (deploymentService, deploymentStatus) =
            createStudyDeployment( createSmartphoneWithConnectedDeviceStudy() )
        val studyDeployment = StudyDeploymentProxy( deploymentService, createDataListener() )
        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val deviceRegistration = smartphone.createRegistration()
        studyDeployment.tryDeployment( study, deviceRegistration )

        // Connected device is not yet registered.
        var status = study.getStatus()
        assertTrue( status is StudyStatus.RegisteringDevices )

        // Once device is registered, deployment succeeds.
        // TODO: It should be possible to register this device through `StudyManager` rather than directly from `deploymentService`.
        deploymentService.registerDevice(
            deploymentStatus.studyDeploymentId,
            connectedDevice.roleName,
            connectedDevice.createRegistration() )
        studyDeployment.tryDeployment( study, deviceRegistration )
        status = study.getStatus()
        assertTrue( status is StudyStatus.Running )
        val registrationStatuses = status.devicesRegistrationStatus
        assertEquals( 2, registrationStatuses.size ) // Smartphone and connected device.
        assertTrue( registrationStatuses[ smartphone ] is DeviceRegistrationStatus.Registered )
        assertTrue( registrationStatuses[ connectedDevice ] is DeviceRegistrationStatus.Registered )
    }

    @Test
    fun tryDeployment_changes_nothing_when_already_deployed() = runSuspendTest {
        // Create a study which instantly deploys because the protocol only contains one primary device.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val studyDeployment = StudyDeploymentProxy( deploymentService, createDataListener() )
        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val deviceRegistration = smartphone.createRegistration()
        studyDeployment.tryDeployment( study, deviceRegistration )
        assertTrue( study.getStatus() is StudyStatus.Running )

        studyDeployment.tryDeployment( study, deviceRegistration )
        val status = study.getStatus()
        assertTrue( status is StudyStatus.Running )
    }

    @Test
    fun tryDeployment_succeeds_when_data_types_of_protocol_measures_are_supported() = runSuspendTest {
        // Create protocol that measures on smartphone and one connected device.
        val protocol = createSmartphoneWithConnectedDeviceStudy()
        val primaryTask = StubTaskConfiguration( "Primary measure", listOf( Measure.DataStream( STUB_DATA_TYPE ) ) )
        protocol.addTaskControl( smartphone.atStartOfStudy().start( primaryTask, smartphone ) )
        val connectedDataType = DataType( "custom", "type" )
        val connectedTask = StubTaskConfiguration( "Connected measure", listOf( Measure.DataStream( connectedDataType ) ) )
        protocol.addTaskControl( smartphone.atStartOfStudy().start( connectedTask, connectedDevice ) )

        // Create a data listener which supports the requested devices and types in the protocol
        val dataListener = DataListener( StubConnectedDeviceDataCollectorFactory(
            localSupportedDataTypes = setOf( STUB_DATA_TYPE ),
            mapOf( StubDeviceConfiguration::class to setOf( connectedDataType ) )
        ) )

        // Create study deployment with preregistered connected device (otherwise study initialization won't complete).
        val (deploymentService, deploymentStatus) = createStudyDeployment( protocol )
        deploymentService.registerDevice(
            deploymentStatus.studyDeploymentId,
            connectedDevice.roleName,
            connectedDevice.createRegistration() )

        // Initializing study for the smartphone deployment should succeed since devices and data types are supported.
        val studyDeployment = StudyDeploymentProxy( deploymentService, dataListener )
        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val deviceRegistration = smartphone.createRegistration()
        studyDeployment.tryDeployment( study, deviceRegistration )
        assertTrue( study.getStatus() is StudyStatus.Running )
    }

    @Test
    fun tryDeployment_fails_when_requested_data_cannot_be_collected() = runSuspendTest {
        // Create a protocol that has one measure.
        val protocol = createSmartphoneStudy()
        val task = StubTaskConfiguration( "One measure", listOf( Measure.DataStream( STUB_DATA_TYPE ) ) )
        protocol.addTaskControl( smartphone.atStartOfStudy().start( task, smartphone ) )

        // Initializing study for the smartphone deployment should fail since StubMeasure can't be collected.
        val (deploymentService, deploymentStatus) = createStudyDeployment( protocol )
        val dataListener = createDataListener( supportedDataTypes = emptyArray() )
        val studyDeployment = StudyDeploymentProxy( deploymentService, dataListener )
        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val deviceRegistration = smartphone.createRegistration()
        assertFailsWith<UnsupportedOperationException>
        {
            studyDeployment.tryDeployment( study, deviceRegistration )
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
        val studyDeployment = StudyDeploymentProxy( deploymentService, dataListener )
        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val deviceRegistration = smartphone.createRegistration()
        assertFailsWith<UnsupportedOperationException>
        {
            studyDeployment.tryDeployment( study, deviceRegistration )
        }
    }

    @Test
    fun stop_succeeds() = runSuspendTest {
        // Initialize a study for a typical 'smartphone study'.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val study = Study( deploymentStatus.studyDeploymentId, smartphone.roleName )
        val studyDeployment = StudyDeploymentProxy( deploymentService, createDataListener() )
        val deviceRegistration = smartphone.createRegistration()
        studyDeployment.tryDeployment( study, deviceRegistration )
        check( study.getStatus() is StudyStatus.Running )

        studyDeployment.stop( study )
        val status = study.getStatus()

        // Study status reflects the study has stopped.
        assertTrue( status is StudyStatus.Stopped )

        // Deployment status also reflects deployment has stopped.
        val newDeploymentStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )
        assertTrue( newDeploymentStatus is StudyDeploymentStatus.Stopped )
    }
}
