package bhrp.studyprotocol.domain.devices

import bhrp.studyprotocol.domain.InvalidConfigurationError
import bhrp.studyprotocol.domain.common.ExtractUniqueKeyMap


/**
 * An initially empty configuration to start defining how a set of devices ([DeviceDescriptor]) work together.
 *
 * Role names within a configuration should be unique.
 */
internal class EmptyDeviceConfiguration : AbstractMap<String, DeviceDescriptor>(), DeviceConfiguration
{
    private val _devices: ExtractUniqueKeyMap<String, DeviceDescriptor> = ExtractUniqueKeyMap(
        { device -> device.roleName },
        InvalidConfigurationError( "Role names of devices within a device configuration should be unique." ) )

    override val entries: Set<Map.Entry<String, DeviceDescriptor>>
        get() = _devices.entries

    private val _connections: MutableMap<DeviceDescriptor, MutableSet<DeviceDescriptor>> = mutableMapOf()

    override val devices: Set<DeviceDescriptor>
        get() { return _devices.values.toSet() }

    override val masterDevices: Set<MasterDeviceDescriptor>
        get() { return _devices.values.filterIsInstance<MasterDeviceDescriptor>().toSet() }

    override fun addMasterDevice( masterDevice: MasterDeviceDescriptor ): Boolean
    {
        val isAdded: Boolean = _devices.tryAddIfKeyIsNew( masterDevice )

        // Add empty 'connections' collection in case it is a new master device.
        if ( isAdded )
        {
            _connections.put( masterDevice, mutableSetOf() )
        }

        return isAdded
    }

    override fun addConnectedDevice( device: DeviceDescriptor, masterDevice: MasterDeviceDescriptor ): Boolean
    {
        verifyMasterDevice( masterDevice )

        // Add device when not yet within this configuration.
        _devices.tryAddIfKeyIsNew( device )

        // Add connection.
        return _connections[ masterDevice ]!!.add( device )
    }

    override fun getConnectedDevices( masterDevice: MasterDeviceDescriptor ): Iterable<DeviceDescriptor>
    {
        verifyMasterDevice( masterDevice )

        return _connections[ masterDevice ] ?: emptyList()
    }

    /**
     * Throws error when master device is not part of this configuration.
     */
    private fun verifyMasterDevice( device: MasterDeviceDescriptor )
    {
        if ( !masterDevices.contains( device ) )
        {
            throw InvalidConfigurationError( "The passed master device is not part of this device configuration." )
        }
    }
}