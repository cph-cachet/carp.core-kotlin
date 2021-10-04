package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor


/**
 * Defines how a set of devices ([DeviceDescriptor]) are configured to work together.
 *
 * Role names within a configuration should be unique.
 */
interface DeviceConfiguration
{
    /**
     * The full list of devices part of this configuration.
     */
    val devices: Set<AnyDeviceDescriptor>

    /**
     * The set of devices which are responsible for aggregating and synchronizing incoming data.
     */
    val masterDevices: Set<AnyMasterDeviceDescriptor>

    /**
     * Add a master device which is responsible for aggregating and synchronizing incoming data.
     *
     * @throws IllegalArgumentException when:
     * - a device with the specified role name already exists
     * - [masterDevice] contains invalid default sampling configurations
     * @param masterDevice A description of the master device to add. Its role name should be unique in the protocol.
     * @return True if the [masterDevice] has been added; false if it is already set as a master device.
     */
    fun addMasterDevice( masterDevice: AnyMasterDeviceDescriptor ): Boolean

    /**
     * Add a device which is connected to a [masterDevice] within this configuration.
     *
     * @throws IllegalArgumentException when:
     *   - a device with the specified role name already exists
     *   - [masterDevice] is not part of the device configuration
     *   - [device] contains invalid default sampling configurations
     * @param device The device to be connected to a master device. Its role name should be unique in the protocol.
     * @return True if the [device] has been added; false if it is already connected to the specified [masterDevice].
     */
    fun addConnectedDevice( device: AnyDeviceDescriptor, masterDevice: AnyMasterDeviceDescriptor ): Boolean

    /**
     * Gets all the devices configured to be connected to the specified [masterDevice].
     *
     * @throws IllegalArgumentException when [masterDevice] is not part of the device configuration.
     * @param includeChainedDevices
     *   Include all underlying devices, i.e., including devices connected through chained master devices.
     */
    fun getConnectedDevices( masterDevice: AnyMasterDeviceDescriptor, includeChainedDevices: Boolean = false ):
        Iterable<AnyDeviceDescriptor>
}
