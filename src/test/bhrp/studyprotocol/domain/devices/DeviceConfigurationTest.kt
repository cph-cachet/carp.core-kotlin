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
}