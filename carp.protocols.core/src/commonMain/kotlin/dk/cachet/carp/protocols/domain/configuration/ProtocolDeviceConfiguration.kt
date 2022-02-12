package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.devices.DeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration


/**
 * Defines how a set of devices ([DeviceConfiguration]) are configured to work together in a protocol.
 *
 * Role names within a configuration should be unique.
 */
interface ProtocolDeviceConfiguration
{
    /**
     * The full list of devices part of this configuration.
     */
    val devices: Set<AnyDeviceConfiguration>

    /**
     * The set of devices which are responsible for aggregating and synchronizing incoming data.
     */
    val primaryDevices: Set<AnyPrimaryDeviceConfiguration>

    /**
     * Add a primary device which is responsible for aggregating and synchronizing incoming data.
     *
     * @throws IllegalArgumentException when:
     * - a device with the specified role name already exists
     * - [primaryDevice] contains invalid default sampling configurations
     * @param primaryDevice A description of the primary device to add. Its role name should be unique in the protocol.
     * @return True if the [primaryDevice] has been added; false if it is already set as a primary device.
     */
    fun addPrimaryDevice( primaryDevice: AnyPrimaryDeviceConfiguration ): Boolean

    /**
     * Add a device which is connected to a [primaryDevice] within this configuration.
     *
     * @throws IllegalArgumentException when:
     *   - a device with the specified role name already exists
     *   - [primaryDevice] is not part of the device configuration
     *   - [device] contains invalid default sampling configurations
     * @param device The device to be connected to a primary device. Its role name should be unique in the protocol.
     * @return True if the [device] has been added; false if it is already connected to the specified [primaryDevice].
     */
    fun addConnectedDevice( device: AnyDeviceConfiguration, primaryDevice: AnyPrimaryDeviceConfiguration ): Boolean

    /**
     * Gets all the devices configured to be connected to the specified [primaryDevice].
     *
     * @throws IllegalArgumentException when [primaryDevice] is not part of the device configuration.
     * @param includeChainedDevices
     *   Include all underlying devices, i.e., including devices connected through chained primary devices.
     */
    fun getConnectedDevices( primaryDevice: AnyPrimaryDeviceConfiguration, includeChainedDevices: Boolean = false ):
        Iterable<AnyDeviceConfiguration>
}
