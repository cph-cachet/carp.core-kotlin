package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceConfiguration
import dk.cachet.carp.common.domain.ExtractUniqueKeyMap


/**
 * An initially empty configuration to start defining how a set of devices ([DeviceConfiguration]) work together.
 *
 * Role names of added [DeviceConfiguration]s should be unique.
 */
@Suppress( "Immutable", "DataClass" )
internal class EmptyProtocolDeviceConfiguration : AbstractMap<String, AnyDeviceConfiguration>(), ProtocolDeviceConfiguration
{
    private val _devices: ExtractUniqueKeyMap<String, AnyDeviceConfiguration> =
        ExtractUniqueKeyMap( { device -> device.roleName } )
        {
            key -> IllegalArgumentException( "Role name \"$key\" is not unique within device configuration." )
        }

    override val entries: Set<Map.Entry<String, AnyDeviceConfiguration>>
        get() = _devices.entries

    private val _connections: MutableMap<AnyDeviceConfiguration, MutableSet<AnyDeviceConfiguration>> = mutableMapOf()

    override val devices: Set<AnyDeviceConfiguration>
        get() = _devices.values.toSet()

    private val _primaryDevices: MutableSet<AnyPrimaryDeviceConfiguration> = mutableSetOf()
    override val primaryDevices: Set<AnyPrimaryDeviceConfiguration>
        get() = _primaryDevices

    override fun addPrimaryDevice( primaryDevice: AnyPrimaryDeviceConfiguration ): Boolean
    {
        verifySamplingConfigurations( primaryDevice )

        val isNewDevice: Boolean = _devices.tryAddIfKeyIsNew( primaryDevice )
        _primaryDevices.add( primaryDevice )

        return isNewDevice
    }

    override fun addConnectedDevice( device: AnyDeviceConfiguration, primaryDevice: AnyPrimaryDeviceConfiguration ): Boolean
    {
        verifySamplingConfigurations( device )
        verifyPrimaryDevice( primaryDevice )

        // Add device when not yet within this configuration.
        _devices.tryAddIfKeyIsNew( device )

        // Add empty 'connections' collection in case it is a primary device without any previous connections, and add.
        return _connections
            .getOrPut( primaryDevice ) { mutableSetOf() }
            .add( device )
    }

    override fun getConnectedDevices( primaryDevice: AnyPrimaryDeviceConfiguration, includeChainedDevices: Boolean ): Iterable<AnyDeviceConfiguration>
    {
        verifyPrimaryDevice( primaryDevice )

        val connectedDevices: MutableList<AnyDeviceConfiguration> = mutableListOf()

        // Add all connections of the primary device.
        if ( primaryDevice in _connections )
        {
            connectedDevices.addAll( _connections[ primaryDevice ]!! )

            // When requested, recursively get all connected devices through chained primary devices.
            if ( includeChainedDevices )
            {
                connectedDevices.filterIsInstance<AnyPrimaryDeviceConfiguration>().forEach {
                    connectedDevices.addAll( getConnectedDevices( it, true ) )
                }
            }
        }

        return connectedDevices
    }

    /**
     * Throws [IllegalArgumentException] when [device] contains one or more invalid sampling configurations.
     */
    private fun verifySamplingConfigurations( device: AnyDeviceConfiguration ) =
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
    private fun verifyPrimaryDevice( device: AnyPrimaryDeviceConfiguration ) = require( device in devices )
        { "The passed primary device with role name \"${device.roleName}\" is not part of this device configuration." }
}
