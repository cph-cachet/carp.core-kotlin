package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.domain.ExtractUniqueKeyMap


/**
 * An initially empty configuration to start defining how a set of devices ([DeviceDescriptor]) work together.
 *
 * Role names of added [DeviceDescriptor]s should be unique.
 */
@Suppress( "Immutable", "DataClass" )
internal class EmptyDeviceConfiguration : AbstractMap<String, AnyDeviceDescriptor>(), DeviceConfiguration
{
    private val _devices: ExtractUniqueKeyMap<String, AnyDeviceDescriptor> =
        ExtractUniqueKeyMap( { device -> device.roleName } )
        {
            key -> IllegalArgumentException( "Role name \"$key\" is not unique within device configuration." )
        }

    override val entries: Set<Map.Entry<String, AnyDeviceDescriptor>>
        get() = _devices.entries

    private val _connections: MutableMap<AnyDeviceDescriptor, MutableSet<AnyDeviceDescriptor>> = mutableMapOf()

    override val devices: Set<AnyDeviceDescriptor>
        get() = _devices.values.toSet()

    private val _masterDevices: MutableSet<AnyMasterDeviceDescriptor> = mutableSetOf()
    override val masterDevices: Set<AnyMasterDeviceDescriptor>
        get() = _masterDevices

    override fun addMasterDevice( masterDevice: AnyMasterDeviceDescriptor ): Boolean
    {
        verifySamplingConfigurations( masterDevice )

        val isNewDevice: Boolean = _devices.tryAddIfKeyIsNew( masterDevice )
        _masterDevices.add( masterDevice )

        return isNewDevice
    }

    override fun addConnectedDevice( device: AnyDeviceDescriptor, masterDevice: AnyMasterDeviceDescriptor ): Boolean
    {
        verifySamplingConfigurations( device )
        verifyMasterDevice( masterDevice )

        // Add device when not yet within this configuration.
        _devices.tryAddIfKeyIsNew( device )

        // Add empty 'connections' collection in case it is a master device without any previous connections, and add.
        return _connections
            .getOrPut( masterDevice ) { mutableSetOf() }
            .add( device )
    }

    override fun getConnectedDevices( masterDevice: AnyMasterDeviceDescriptor, includeChainedDevices: Boolean ): Iterable<AnyDeviceDescriptor>
    {
        verifyMasterDevice( masterDevice )

        val connectedDevices: MutableList<AnyDeviceDescriptor> = mutableListOf()

        // Add all connections of the master device.
        if ( masterDevice in _connections )
        {
            connectedDevices.addAll( _connections[ masterDevice ]!! )

            // When requested, recursively get all connected devices through chained master devices.
            if ( includeChainedDevices )
            {
                connectedDevices.filterIsInstance<AnyMasterDeviceDescriptor>().forEach {
                    connectedDevices.addAll( getConnectedDevices( it, true ) )
                }
            }
        }

        return connectedDevices
    }

    /**
     * Throws [IllegalArgumentException] when [device] contains one or more invalid sampling configurations.
     */
    private fun verifySamplingConfigurations( device: AnyDeviceDescriptor ) =
        try { device.validateDefaultSamplingConfiguration() }
        catch ( ex: IllegalStateException )
        {
            throw IllegalArgumentException(
                "The device with role name `${device.roleName}` contains an invalid sampling configuration.",
                ex
            )
        }

    /**
     * Throws [IllegalArgumentException] when [device] is not part of this configuration.
     */
    private fun verifyMasterDevice( device: AnyMasterDeviceDescriptor ) = require( device in devices )
        { "The passed master device with role name \"${device.roleName}\" is not part of this device configuration." }
}
