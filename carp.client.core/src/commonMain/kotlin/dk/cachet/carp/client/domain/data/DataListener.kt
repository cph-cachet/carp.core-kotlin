package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import kotlin.reflect.KClass


/**
 * Allows subscribing to [Data] of requested [DataType]s on a master device and connected devices.
 */
interface DataListener
{
    /**
     * Determines whether subscribing to [Data] of a given [dataType] is supported on this master device.
     */
    fun supportsData( dataType: DataType ): Boolean

    /**
     * Determines whether subscribing to [Data] of a given [dataType] is supported
     * by connecting to a device of type [connectedDeviceType] using [deviceRegistration].
     */
    fun supportsDataOnConnectedDevice(
        dataType: DataType,
        connectedDeviceType: KClass<out AnyDeviceDescriptor>,
        deviceRegistration: DeviceRegistration?
    ): Boolean
}
