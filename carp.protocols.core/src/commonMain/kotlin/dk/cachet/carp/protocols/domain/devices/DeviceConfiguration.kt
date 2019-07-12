package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.InvalidConfigurationError


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
    val devices: Set<DeviceDescriptor<*>>

    /**
     * The set of devices which are responsible for aggregating and synchronizing incoming data.
     */
    val masterDevices: Set<MasterDeviceDescriptor<*>>

    /**
     * Add a master device which is responsible for aggregating and synchronizing incoming data.
     *
     * Throws an [InvalidConfigurationError] in case a device with the specified role name already exists.
     *
     * @param masterDevice A description of the master device to add. Its role name should be unique in the protocol.
     * @return True if the device has been added; false if the specified [MasterDeviceDescriptor] is already set as a master device.
     */
    fun addMasterDevice( masterDevice: MasterDeviceDescriptor<*> ): Boolean

    /**
     * Add a device which is connected to a master device within this configuration.
     *
     * Throws an [InvalidConfigurationError] in case a device with the specified role name already exists.
     *
     * @param device The device to be connected to a master device. Its role name should be unique in the protocol.
     * @param masterDevice The master device to connect to.
     * @return True if the device has been added; false if the specified [DeviceDescriptor] is already connected to the specified [MasterDeviceDescriptor].
     */
    fun addConnectedDevice( device: DeviceDescriptor<*>, masterDevice: MasterDeviceDescriptor<*> ): Boolean

    /**
     * Gets all the devices configured to be connected to the specified [MasterDeviceDescriptor].
     *
     * @param masterDevice The master device for which to return the connected devices.
     * @param includeChainedDevices Include all underlying devices, i.e., including devices connected through chained master devices.
     */
    fun getConnectedDevices( masterDevice: MasterDeviceDescriptor<*>, includeChainedDevices: Boolean = false ): Iterable<DeviceDescriptor<*>>
}