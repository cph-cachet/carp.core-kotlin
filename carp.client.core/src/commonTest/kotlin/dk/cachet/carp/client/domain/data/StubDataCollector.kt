package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import kotlin.reflect.KClass


/**
 * A stub for [DataCollector] which presumes data can be collected for all requested data types.
 */
class StubDataCollector : DataCollector
{
    override fun canCollectData( dataType: DataType ): Boolean = true

    override fun canCollectDataForConnectedDevice(
        dataType: DataType,
        connectedDeviceType: KClass<out AnyDeviceDescriptor>,
        deviceRegistration: DeviceRegistration?
    ): Boolean = true
}
