package dk.cachet.carp.common.domain.devices

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import kotlin.test.*


/**
 * Base class with tests for [DeviceConfiguration] which can be used to test extending types.
 */
interface DeviceConfigurationTest
{
    /**
     * Called for each test to create a device configuration to run tests on.
     */
    fun createDeviceConfiguration(): DeviceConfiguration


    @Test
    fun addMasterDevice_succeeds()
    {
        val configuration = createDeviceConfiguration()
        val masterDevice = StubMasterDeviceDescriptor()

        val isAdded = configuration.addMasterDevice( masterDevice )
        assertTrue( isAdded )
        assertTrue( configuration.masterDevices.contains( masterDevice ) )
    }

    @Test
    fun only_devices_added_as_addMasterDevice_show_up_in_masterdevices()
    {
        val configuration = createDeviceConfiguration()
        val masterDevice = StubMasterDeviceDescriptor()
        val chainedMasterDevice = StubMasterDeviceDescriptor( "Chained" )

        configuration.addMasterDevice( masterDevice )
        configuration.addConnectedDevice( chainedMasterDevice, masterDevice )
        assertEquals( 1, configuration.masterDevices.count() )
    }

    @Test
    fun addConnectedDevice_succeeds()
    {
        val configuration = createDeviceConfiguration()
        val masterDevice = StubMasterDeviceDescriptor()
        val connectedDevice = StubDeviceDescriptor()
        configuration.addMasterDevice( masterDevice )
        assertEquals( 0, configuration.getConnectedDevices( masterDevice ).count() )

        val isAdded: Boolean = configuration.addConnectedDevice( connectedDevice, masterDevice )
        assertTrue( isAdded )
        assertEquals( connectedDevice, configuration.getConnectedDevices( masterDevice ).single() )
    }

    @Test
    fun devices_lists_the_full_list_of_devices_without_duplicates()
    {
        val configuration = createDeviceConfiguration()
        val masterDevice1 = StubMasterDeviceDescriptor( "Device 1" )
        val masterDevice2 = StubMasterDeviceDescriptor( "Device 2" )
        val connectedDevice = StubDeviceDescriptor( "Device 3" )
        with ( configuration )
        {
            addMasterDevice( masterDevice1 )
            addMasterDevice( masterDevice2 )
            addConnectedDevice( connectedDevice, masterDevice1 )
            addConnectedDevice( connectedDevice, masterDevice2 )
        }

        assertEquals( 3, configuration.devices.count() )
        assertTrue( configuration.devices.contains( masterDevice1 ) )
        assertTrue( configuration.devices.contains( masterDevice2 ) )
        assertTrue( configuration.devices.contains( connectedDevice ) )
    }

    @Test
    fun addMasterDevice_multiple_times_only_adds_first_time()
    {
        val configuration = createDeviceConfiguration()
        val masterDevice = StubMasterDeviceDescriptor()
        configuration.addMasterDevice( masterDevice )

        val isAdded: Boolean = configuration.addMasterDevice( masterDevice )
        assertFalse( isAdded )
        assertEquals( 1, configuration.masterDevices.count() )
    }

    @Test
    fun addConnectedDevice_multiple_times_only_adds_first_time()
    {
        val configuration = createDeviceConfiguration()
        val masterDevice = StubMasterDeviceDescriptor()
        val connectedDevice = StubDeviceDescriptor()
        configuration.addMasterDevice( masterDevice )
        configuration.addConnectedDevice( connectedDevice, masterDevice )

        val isAdded: Boolean = configuration.addConnectedDevice( connectedDevice, masterDevice )
        assertFalse( isAdded )
        assertEquals( 1, configuration.getConnectedDevices( masterDevice ).count() )
    }

    @Test
    fun do_not_allow_duplicate_role_names_for_devices()
    {
        val configuration = createDeviceConfiguration()
        val masterDevice = StubMasterDeviceDescriptor( "Unique name" )
        configuration.addMasterDevice( masterDevice )
        configuration.addConnectedDevice( StubDeviceDescriptor( "Duplicate name" ), masterDevice )

        // Adding an additional master device with duplicate name should fail.
        assertFailsWith<IllegalArgumentException>
        {
            configuration.addMasterDevice( StubMasterDeviceDescriptor( "Duplicate name" ) )
        }
    }

    @Test
    fun cant_addConnectedDevice_for_nonexisting_master_device()
    {
        val configuration = createDeviceConfiguration()

        assertFailsWith<IllegalArgumentException>
        {
            configuration.addConnectedDevice( StubDeviceDescriptor(), StubMasterDeviceDescriptor() )
        }
    }

    @Test
    fun cant_getConnectedDevices_for_nonexisting_master_device()
    {
        val configuration = createDeviceConfiguration()

        assertFailsWith<IllegalArgumentException>
        {
            configuration.getConnectedDevices( StubMasterDeviceDescriptor() )
        }
    }

    @Test
    fun getConnectedDevices_with_includeChainedDevices_returns_all_underlying_devices()
    {
        val configuration = createDeviceConfiguration()
        val master1 = StubMasterDeviceDescriptor( "Master 1" )
        val master2 = StubMasterDeviceDescriptor( "Master 2" )
        val connected = StubDeviceDescriptor( "Connected" )
        with ( configuration )
        {
            addMasterDevice( master1 )
            addConnectedDevice( master2, master1 )
            addConnectedDevice( connected, master2 )
        }

        val underlyingDevices: Iterable<AnyDeviceDescriptor> = configuration.getConnectedDevices( master1, true )
        val expectedDevices = listOf( master2, connected )
        assertEquals( expectedDevices.count(), underlyingDevices.intersect( expectedDevices ).count() )
    }
}
