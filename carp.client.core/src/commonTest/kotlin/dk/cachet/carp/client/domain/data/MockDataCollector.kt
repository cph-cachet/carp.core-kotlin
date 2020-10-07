package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import kotlin.reflect.KClass


/**
 * A mock for [DataCollector] which can specify whether data collection for requested data types is supported or not.
 *
 * @param supportsDataCollection
 *   Determines whether or not this [DataCollector] supports data collection for any requested type. True by default.
 */
class MockDataCollector( private val supportsDataCollection: Boolean = true ) : DataCollector
{
    override fun supportsDataCollection( dataType: DataType ): Boolean = supportsDataCollection

    override fun supportsDataCollectionOnConnectedDevice(
        dataType: DataType,
        connectedDeviceType: KClass<out AnyDeviceDescriptor>,
        deviceRegistration: DeviceRegistration?
    ): Boolean = supportsDataCollection
}
