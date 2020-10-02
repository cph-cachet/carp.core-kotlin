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
     * Determines whether a given [dataType] can be collected on this master device.
     */
    fun canCollectData( dataType: DataType ): Boolean

    /**
     * Determines whether a given [dataType] can be collected by connecting to a device of type [connectedDeviceType] using [deviceRegistration].
     */
    fun canCollectDataForConnectedDevice(
        dataType: DataType,
        connectedDeviceType: KClass<out AnyDeviceDescriptor>,
        deviceRegistration: DeviceRegistration?
    ): Boolean
}
