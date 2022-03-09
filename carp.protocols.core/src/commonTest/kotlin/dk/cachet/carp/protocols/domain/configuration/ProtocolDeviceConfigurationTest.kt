package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import kotlin.test.*


/**
 * Base class with tests for [ProtocolDeviceConfiguration] which can be used to test extending types.
 */
interface ProtocolDeviceConfigurationTest
{
    /**
     * Called for each test to create a device configuration to run tests on.
     */
    fun createDeviceConfiguration(): ProtocolDeviceConfiguration


    @Test
    fun addPrimaryDevice_succeeds()
    {
        val configuration = createDeviceConfiguration()
        val primaryDevice = StubPrimaryDeviceConfiguration()

        val isAdded = configuration.addPrimaryDevice( primaryDevice )
        assertTrue( isAdded )
        assertTrue( configuration.primaryDevices.contains( primaryDevice ) )
    }

    @Test
    fun only_devices_added_as_addPrimaryDevice_show_up_in_primarydevices()
    {
        val configuration = createDeviceConfiguration()
        val primaryDevice = StubPrimaryDeviceConfiguration()
        val chainedPrimaryDevice = StubPrimaryDeviceConfiguration( "Chained" )

        configuration.addPrimaryDevice( primaryDevice )
        configuration.addConnectedDevice( chainedPrimaryDevice, primaryDevice )
        assertEquals( 1, configuration.primaryDevices.count() )
    }

    @Test
    fun addConnectedDevice_succeeds()
    {
        val configuration = createDeviceConfiguration()
        val primaryDevice = StubPrimaryDeviceConfiguration()
        val connectedDevice = StubDeviceConfiguration()
        configuration.addPrimaryDevice( primaryDevice )
        assertEquals( 0, configuration.getConnectedDevices( primaryDevice ).count() )

        val isAdded: Boolean = configuration.addConnectedDevice( connectedDevice, primaryDevice )
        assertTrue( isAdded )
        assertEquals( connectedDevice, configuration.getConnectedDevices( primaryDevice ).single() )
    }

    @Test
    fun devices_lists_the_full_list_of_devices_without_duplicates()
    {
        val configuration = createDeviceConfiguration()
        val primaryDevice1 = StubPrimaryDeviceConfiguration( "Device 1" )
        val primaryDevice2 = StubPrimaryDeviceConfiguration( "Device 2" )
        val connectedDevice = StubDeviceConfiguration( "Device 3" )
        with ( configuration )
        {
            addPrimaryDevice( primaryDevice1 )
            addPrimaryDevice( primaryDevice2 )
            addConnectedDevice( connectedDevice, primaryDevice1 )
            addConnectedDevice( connectedDevice, primaryDevice2 )
        }

        assertEquals( 3, configuration.devices.count() )
        assertTrue( configuration.devices.contains( primaryDevice1 ) )
        assertTrue( configuration.devices.contains( primaryDevice2 ) )
        assertTrue( configuration.devices.contains( connectedDevice ) )
    }

    @Test
    fun addPrimaryDevice_multiple_times_only_adds_first_time()
    {
        val configuration = createDeviceConfiguration()
        val primaryDevice = StubPrimaryDeviceConfiguration()
        configuration.addPrimaryDevice( primaryDevice )

        val isAdded: Boolean = configuration.addPrimaryDevice( primaryDevice )
        assertFalse( isAdded )
        assertEquals( 1, configuration.primaryDevices.count() )
    }

    @Test
    fun addConnectedDevice_multiple_times_only_adds_first_time()
    {
        val configuration = createDeviceConfiguration()
        val primaryDevice = StubPrimaryDeviceConfiguration()
        val connectedDevice = StubDeviceConfiguration()
        configuration.addPrimaryDevice( primaryDevice )
        configuration.addConnectedDevice( connectedDevice, primaryDevice )

        val isAdded: Boolean = configuration.addConnectedDevice( connectedDevice, primaryDevice )
        assertFalse( isAdded )
        assertEquals( 1, configuration.getConnectedDevices( primaryDevice ).count() )
    }

    @Test
    fun do_not_allow_duplicate_role_names_for_devices()
    {
        val configuration = createDeviceConfiguration()
        val primaryDevice = StubPrimaryDeviceConfiguration( "Unique name" )
        configuration.addPrimaryDevice( primaryDevice )
        configuration.addConnectedDevice( StubDeviceConfiguration( "Duplicate name" ), primaryDevice )

        // Adding an additional primary device with duplicate name should fail.
        assertFailsWith<IllegalArgumentException>
        {
            configuration.addPrimaryDevice( StubPrimaryDeviceConfiguration( "Duplicate name" ) )
        }
    }

    @Test
    fun cant_addConnectedDevice_for_nonexisting_primary_device()
    {
        val configuration = createDeviceConfiguration()

        assertFailsWith<IllegalArgumentException>
        {
            configuration.addConnectedDevice( StubDeviceConfiguration(), StubPrimaryDeviceConfiguration() )
        }
    }

    @Test
    fun cant_getConnectedDevices_for_nonexisting_primary_device()
    {
        val configuration = createDeviceConfiguration()

        assertFailsWith<IllegalArgumentException>
        {
            configuration.getConnectedDevices( StubPrimaryDeviceConfiguration() )
        }
    }

    @Test
    fun getConnectedDevices_with_includeChainedDevices_returns_all_underlying_devices()
    {
        val configuration = createDeviceConfiguration()
        val primary1 = StubPrimaryDeviceConfiguration( "Primary 1" )
        val primary2 = StubPrimaryDeviceConfiguration( "Primary 2" )
        val connected = StubDeviceConfiguration( "Connected" )
        with ( configuration )
        {
            addPrimaryDevice( primary1 )
            addConnectedDevice( primary2, primary1 )
            addConnectedDevice( connected, primary2 )
        }

        val underlyingDevices: Iterable<AnyDeviceConfiguration> = configuration.getConnectedDevices( primary1, true )
        val expectedDevices = setOf( primary2, connected )
        assertEquals( expectedDevices.count(), underlyingDevices.intersect( expectedDevices ).count() )
    }
}
