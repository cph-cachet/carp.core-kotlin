package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.ExtractUniqueKeyMap
import dk.cachet.carp.protocols.domain.InvalidConfigurationError


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

    private val _masterDevices: MutableSet<MasterDeviceDescriptor> = mutableSetOf()
    override val masterDevices: Set<MasterDeviceDescriptor>
        get() { return _masterDevices }

    override fun addMasterDevice( masterDevice: MasterDeviceDescriptor ): Boolean
    {
        val isNewDevice: Boolean = _devices.tryAddIfKeyIsNew( masterDevice )
        _masterDevices.add( masterDevice )

        return isNewDevice
    }

    override fun addConnectedDevice( device: DeviceDescriptor, masterDevice: MasterDeviceDescriptor ): Boolean
    {
        verifyMasterDevice( masterDevice )

        // Add device when not yet within this configuration.
        _devices.tryAddIfKeyIsNew( device )

        // Add empty 'connections' collection in case it is a master device without any previous connections.
        if ( !_connections.contains( masterDevice ) )
        {
            _connections.put( masterDevice, mutableSetOf() )
        }

        return _connections[ masterDevice ]!!.add( device )
    }

    override fun getConnectedDevices( masterDevice: MasterDeviceDescriptor, includeChainedDevices: Boolean ): Iterable<DeviceDescriptor>
    {
        verifyMasterDevice( masterDevice )

        val connectedDevices: MutableList<DeviceDescriptor> = mutableListOf()

        // Add all connections of the master device.
        if ( _connections.contains( masterDevice ) )
        {
            connectedDevices.addAll( _connections[ masterDevice ]!! )

            // When requested, recursively get all connected devices through chained master devices.
            if ( includeChainedDevices )
            {
                connectedDevices.filterIsInstance<MasterDeviceDescriptor>().forEach {
                    connectedDevices.addAll( getConnectedDevices( it, true ) )
                }
            }
        }

        return connectedDevices
    }

    /**
     * Throws error when master device is not part of this configuration.
     */
    private fun verifyMasterDevice( device: MasterDeviceDescriptor )
    {
        if ( !devices.contains( device ) )
        {
            throw InvalidConfigurationError( "The passed master device is not part of this device configuration." )
        }
    }
}