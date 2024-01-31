package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.sampling.BatteryAwareSamplingConfiguration
import dk.cachet.carp.common.application.sampling.Granularity
import dk.cachet.carp.common.application.sampling.GranularitySamplingConfiguration
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingConfiguration
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTriggerConfiguration
import kotlin.test.*


/**
 * Tests for [PrimaryDeviceDeployment].
 */
class PrimaryDeviceDeploymentTest
{
    @Test
    fun getRuntimeDeviceInfo_contains_all_devices()
    {
        val primary = StubPrimaryDeviceConfiguration( "Primary" )
        val registration = primary.createRegistration()
        val connected = StubDeviceConfiguration( "Connected" )

        // Deployment with registered primary device and unregistered connected device.
        val deployment = PrimaryDeviceDeployment( primary, registration, setOf( connected ) )

        val devices = deployment.getRuntimeDeviceInfo()
        assertEquals( 2, devices.size )
        assertEquals( 1, devices.count { it.configuration == primary && !it.isConnectedDevice } )
        assertEquals( 1, devices.count { it.configuration == connected && it.isConnectedDevice } )
    }

    @Test
    fun getRuntimeDeviceInfo_contains_default_sampling_configuration()
    {
        val primary = StubPrimaryDeviceConfiguration( "Primary" )
        val registration = primary.createRegistration()
        val deployment = PrimaryDeviceDeployment( primary, registration )

        val deviceInfo = deployment.getRuntimeDeviceInfo()
            .first { it.configuration == primary }
        assertEquals(
            StubPrimaryDeviceConfiguration.Sensors.map { it.key to it.value.default }.toMap(),
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
        val device = Smartphone( "Irrelevant", false, mapOf( dataType to configurationOverride ) )
        val registration = device.createRegistration()
        val deployment = PrimaryDeviceDeployment( device, registration )

        val deviceInfo = deployment.getRuntimeDeviceInfo()
            .first { it.configuration == device }
        assertEquals(
            configurationOverride,
            deviceInfo.defaultSamplingConfiguration[ dataType ]
        )
    }

    @Test
    fun getRuntimeDeviceInfo_contains_unexpected_data_type_sampling_configurations()
    {
        val unexpectedType = DataType( "something", "unexpected" )
        val unexpectedTypeConfiguration = NoOptionsSamplingConfiguration
        val primary =
            StubPrimaryDeviceConfiguration( "Primary", false, mapOf( unexpectedType to unexpectedTypeConfiguration ) )
        val registration = primary.createRegistration()
        val deployment = PrimaryDeviceDeployment( primary, registration )

        val deviceInfo = deployment.getRuntimeDeviceInfo()
            .first { it.configuration == primary }
        assertEquals( unexpectedTypeConfiguration, deviceInfo.defaultSamplingConfiguration[ unexpectedType ] )
    }

    @Test
    fun getRuntimeDeviceInfo_contains_all_tasks()
    {
        val device = StubPrimaryDeviceConfiguration( "Primary" )
        val registration = device.createRegistration()
        val connected = StubDeviceConfiguration( "Connected" )
        val connectedRegistration = connected.createRegistration()
        val task = StubTaskConfiguration()
        val primaryTrigger = StubTriggerConfiguration( device.roleName )
        val connectedTrigger = StubTriggerConfiguration( connected.roleName )

        val deployment = PrimaryDeviceDeployment(
            deviceConfiguration = device,
            registration = registration,
            connectedDevices = setOf( connected ),
            connectedDeviceRegistrations = mapOf( connected.roleName to connectedRegistration ),
            tasks = setOf( task ),
            triggers = mapOf( 0 to primaryTrigger, 1 to connectedTrigger ),
            taskControls = setOf(
                TaskControl( 0, task.name, device.roleName, TaskControl.Control.Start ),
                TaskControl( 1, task.name, connected.roleName, TaskControl.Control.Start )
            )
        )
        val devices: List<PrimaryDeviceDeployment.RuntimeDeviceInfo> = deployment.getRuntimeDeviceInfo()

        assertEquals( 2, devices.size )
        assertEquals( setOf( task ), devices.first { it.configuration == device }.tasks )
        assertEquals( setOf( task ), devices.first { it.configuration == connected }.tasks )
    }


    @Test
    fun getRuntimeDeviceInfo_includes_devices_with_no_tasks()
    {
        val device = StubPrimaryDeviceConfiguration( "Primary" )
        val registration = device.createRegistration()

        val deployment = PrimaryDeviceDeployment( device, registration )
        val devices: List<PrimaryDeviceDeployment.RuntimeDeviceInfo> = deployment.getRuntimeDeviceInfo()

        assertEquals( 1, devices.size )
        assertEquals( emptySet(), devices.single().tasks )
    }

    @Test
    fun getRuntimeDeviceInfo_does_not_include_tasks_for_other_primary_devices()
    {
        val primary1 = StubPrimaryDeviceConfiguration( "Primary 1" )
        val task = StubTaskConfiguration()
        val primary2 = StubPrimaryDeviceConfiguration( "Primary 2" )
        val primary1Registration = primary1.createRegistration()
        val primary1Trigger = StubTriggerConfiguration( primary1.roleName )

        val deployment = PrimaryDeviceDeployment(
            deviceConfiguration = primary1,
            registration = primary1Registration,
            connectedDevices = emptySet(),
            connectedDeviceRegistrations = emptyMap(),
            tasks = setOf( task ),
            triggers = mapOf( 0 to primary1Trigger ),
            taskControls = setOf(
                TaskControl( 0, task.name, primary1.roleName, TaskControl.Control.Start ),
                TaskControl( 0, "Task on Primar 2", primary2.roleName, TaskControl.Control.Start )
            )
        )
        val devices: List<PrimaryDeviceDeployment.RuntimeDeviceInfo> = deployment.getRuntimeDeviceInfo()

        assertEquals( 1, devices.size ) // The other primary device (primary2) is not included.
        assertEquals( primary1, devices.single().configuration )
        assertEquals( setOf( task ), devices.single().tasks )
    }
}
