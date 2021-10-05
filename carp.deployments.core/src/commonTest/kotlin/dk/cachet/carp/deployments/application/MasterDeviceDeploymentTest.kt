package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.sampling.BatteryAwareSamplingConfiguration
import dk.cachet.carp.common.application.sampling.Granularity
import dk.cachet.carp.common.application.sampling.GranularitySamplingConfiguration
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTrigger
import kotlin.test.*


/**
 * Tests for [MasterDeviceDeployment].
 */
class MasterDeviceDeploymentTest
{
    @Test
    fun getRuntimeDeviceInfo_contains_all_devices()
    {
        val master = StubMasterDeviceDescriptor( "Master" )
        val registration = master.createRegistration()
        val connected = StubDeviceDescriptor( "Connected" )

        // Deployment with registered master device and unregistered connected device.
        val deployment = MasterDeviceDeployment( master, registration, setOf( connected ) )

        val devices = deployment.getRuntimeDeviceInfo()
        assertEquals( 2, devices.size )
        assertEquals( 1, devices.count { it.descriptor == master && !it.isConnectedDevice } )
        assertEquals( 1, devices.count { it.descriptor == connected && it.isConnectedDevice } )
    }

    @Test
    fun getRuntimeDeviceInfo_contains_default_sampling_configuration()
    {
        val master = StubMasterDeviceDescriptor( "Master" )
        val registration = master.createRegistration()
        val deployment = MasterDeviceDeployment( master, registration )

        val deviceInfo = deployment.getRuntimeDeviceInfo()
            .first { it.descriptor == master }
        assertEquals(
            StubMasterDeviceDescriptor.Sensors.map { it.key to it.value.default }.toMap(),
            deviceInfo.defaultSamplingConfiguration
        )
    }

    @Test
    fun getRuntimeDeviceInfo_contains_overridden_sampling_configuration()
    {
        val typeMetaData = Smartphone.Sensors.GEOLOCATION
        val dataType = typeMetaData.dataType.type
        val configurationOverride = BatteryAwareSamplingConfiguration(
            GranularitySamplingConfiguration( Granularity.Coarse ),
            GranularitySamplingConfiguration( Granularity.Coarse ),
            GranularitySamplingConfiguration( Granularity.Coarse )
        )
        val device = Smartphone( "Irrelevant", mapOf( dataType to configurationOverride ) )
        val registration = device.createRegistration()
        val deployment = MasterDeviceDeployment( device, registration )

        val deviceInfo = deployment.getRuntimeDeviceInfo()
            .first { it.descriptor == device }
        assertEquals(
            configurationOverride,
            deviceInfo.defaultSamplingConfiguration[ dataType ]
        )
    }

    @Test
    fun getTasksPerDevice_contains_all_tasks()
    {
        val device = StubMasterDeviceDescriptor( "Master" )
        val registration = device.createRegistration()
        val connected = StubDeviceDescriptor( "Connected" )
        val connectedRegistration = connected.createRegistration()
        val task = StubTaskDescriptor()
        val masterTrigger = StubTrigger( device.roleName )
        val connectedTrigger = StubTrigger( connected.roleName )

        val deployment = MasterDeviceDeployment(
            deviceDescriptor = device,
            configuration = registration,
            connectedDevices = setOf( connected ),
            connectedDeviceConfigurations = mapOf( connected.roleName to connectedRegistration ),
            tasks = setOf( task ),
            triggers = mapOf( 0 to masterTrigger, 1 to connectedTrigger ),
            taskControls = setOf(
                TaskControl( 0, task.name, device.roleName, TaskControl.Control.Start ),
                TaskControl( 1, task.name, connected.roleName, TaskControl.Control.Start )
            )
        )
        val deviceTasks: List<MasterDeviceDeployment.DeviceTasks> = deployment.getTasksPerDevice()

        assertEquals( 2, deviceTasks.size )
        assertEquals( setOf( task ), deviceTasks.first { it.device.descriptor == device }.tasks )
        assertEquals( setOf( task ), deviceTasks.first { it.device.descriptor == connected }.tasks )
    }


    @Test
    fun getTasksPerDevice_includes_devices_with_no_tasks()
    {
        val device = StubMasterDeviceDescriptor( "Master" )
        val registration = device.createRegistration()

        val deployment = MasterDeviceDeployment( deviceDescriptor = device, configuration = registration )
        val tasks: List<MasterDeviceDeployment.DeviceTasks> = deployment.getTasksPerDevice()

        assertEquals( 1, tasks.size )
        assertEquals( emptySet(), tasks.single().tasks )
    }

    @Test
    fun getTaskPerDevice_does_not_include_tasks_for_other_master_devices()
    {
        val master1 = StubMasterDeviceDescriptor( "Master 1" )
        val task = StubTaskDescriptor()
        val master2 = StubMasterDeviceDescriptor( "Master 2" )
        val master1Registration = master1.createRegistration()
        val master1Trigger = StubTrigger( master1.roleName )

        val deployment = MasterDeviceDeployment(
            deviceDescriptor = master1,
            configuration = master1Registration,
            connectedDevices = emptySet(),
            connectedDeviceConfigurations = emptyMap(),
            tasks = setOf( task ),
            triggers = mapOf( 0 to master1Trigger ),
            taskControls = setOf(
                TaskControl( 0, task.name, master1.roleName, TaskControl.Control.Start ),
                TaskControl( 0, "Task on Master 2", master2.roleName, TaskControl.Control.Start )
            )
        )
        val tasks: List<MasterDeviceDeployment.DeviceTasks> = deployment.getTasksPerDevice()

        assertEquals( 1, tasks.size ) // The other master device (master2) is not included.
        assertEquals( master1, tasks.single().device.descriptor )
        assertEquals( setOf( task ), tasks.single().tasks )
    }
}
