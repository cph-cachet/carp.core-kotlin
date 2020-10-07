package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import kotlin.reflect.KClass


/**
 * Manage [Data] collection of requested [DataType]s on a master device and connected devices.
 */
interface DataCollector
{
    /**
     * Determines whether data collection for a given [dataType] is supported on this master device.
     */
    fun supportsDataCollection( dataType: DataType ): Boolean

    /**
     * Determines whether data collection for a given [dataType] by connecting to a device of type [connectedDeviceType] using [deviceRegistration] is supported.
     */
    fun supportsDataCollectionOnConnectedDevice(
        dataType: DataType,
        connectedDeviceType: KClass<out AnyDeviceDescriptor>,
        deviceRegistration: DeviceRegistration?
    ): Boolean
}
