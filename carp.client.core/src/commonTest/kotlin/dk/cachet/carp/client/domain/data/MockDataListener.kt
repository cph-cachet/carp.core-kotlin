package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import kotlin.reflect.KClass


/**
 * A mock for [DataListener] which can specify whether subscribing to data for requested data types is supported or not.
 *
 * @param supportsData
 *   Determines whether or not this [DataListener] supports subscribing to data for any requested type. True by default.
 */
class MockDataListener( private val supportsData: Boolean = true ) : DataListener
{
    override fun supportsData( dataType: DataType ): Boolean = supportsData

    override fun supportsDataOnConnectedDevice(
        dataType: DataType,
        connectedDeviceType: KClass<out AnyDeviceDescriptor>,
        deviceRegistration: DeviceRegistration?
    ): Boolean = supportsData
}
