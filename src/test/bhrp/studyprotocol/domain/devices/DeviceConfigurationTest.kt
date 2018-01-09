package bhrp.studyprotocol.domain.devices

import bhrp.studyprotocol.domain.InvalidConfigurationError
import org.junit.jupiter.api.*
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
    fun `addMasterDevice succeeds`()
    {
        val configuration = createDeviceConfiguration()
        val masterDevice = StubMasterDeviceDescriptor()

        val isAdded = configuration.addMasterDevice( masterDevice )
        assertTrue( isAdded )
        assertTrue( configuration.masterDevices.contains( masterDevice ) )
    }

    @Test
    fun `addConnectedDevice succeeds`()
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
    fun `devices lists the full list of devices without duplicates`()
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
    fun `addMasterDevice multiple times only adds first time`()
    {
        val configuration = createDeviceConfiguration()
        val masterDevice = StubMasterDeviceDescriptor()
        configuration.addMasterDevice( masterDevice )

        val isAdded: Boolean = configuration.addMasterDevice( masterDevice )
        assertFalse( isAdded )
        assertEquals( 1, configuration.masterDevices.count() )
    }

    @Test
    fun `addConnectedDevice multiple times only adds first time`()
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
    fun `do not allow duplicate role names for devices`()
    {
        val configuration = createDeviceConfiguration()
        val masterDevice = StubMasterDeviceDescriptor( "Unique name" )
        configuration.addMasterDevice( masterDevice )
        configuration.addConnectedDevice( StubDeviceDescriptor( "Duplicate name" ), masterDevice )

        // Adding an additional master device with duplicate name should fail.
        assertFailsWith<InvalidConfigurationError>
        {
            configuration.addMasterDevice( StubMasterDeviceDescriptor( "Duplicate name" ) )
        }
    }

    @Test
    fun `can't addConnectedDevice for non-existing master device`()
    {
        val configuration = createDeviceConfiguration()

        assertFailsWith<InvalidConfigurationError>
        {
            configuration.addConnectedDevice( StubDeviceDescriptor(), StubMasterDeviceDescriptor() )
        }
    }

    @Test
    fun `can't getConnectedDevices for non-existing master device`()
    {
        val configuration = createDeviceConfiguration()

        assertFailsWith<InvalidConfigurationError>
        {
            configuration.getConnectedDevices( StubMasterDeviceDescriptor() )
        }
    }

    @Test
    fun `getConnectedDevices with includeChainedDevices returns all underlying devices`()
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

        val underlyingDevices: Iterable<DeviceDescriptor> = configuration.getConnectedDevices( master1, true )
        val expectedDevices = listOf( master2, connected )
        assertEquals( expectedDevices.count(), underlyingDevices.intersect( expectedDevices ).count() )
    }
}