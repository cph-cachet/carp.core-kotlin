package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import kotlin.reflect.KClass


/**
 * A mock for [DataCollector] which can specify whether data can be collected for requested data types or not.
 *
 * @param canCollectData
 *   Determines whether or not this [DataCollector] can collect data for any requested type. True by default.
 */
class MockDataCollector( private val canCollectData: Boolean = true ) : DataCollector
{
    override fun canCollectData( dataType: DataType ): Boolean = canCollectData

    override fun canCollectDataForConnectedDevice(
        dataType: DataType,
        connectedDeviceType: KClass<out AnyDeviceDescriptor>,
        deviceRegistration: DeviceRegistration?
    ): Boolean = canCollectData
}
