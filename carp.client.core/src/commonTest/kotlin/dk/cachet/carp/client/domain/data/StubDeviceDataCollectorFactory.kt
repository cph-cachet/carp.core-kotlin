package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceType


/**
 * A [DeviceDataCollectorFactory] which for all [DeviceDataCollector] instances
 * uses [StubDeviceDataCollector] with the specified [supportedDataTypes].
 */
class StubDeviceDataCollectorFactory( private val supportedDataTypes: Set<DataType> ) :
    DeviceDataCollectorFactory( StubDeviceDataCollector( supportedDataTypes ) )
{
    override fun createConnectedDataCollector(
        deviceType: DeviceType,
        deviceRegistration: DeviceRegistration
    ): ConnectedDeviceDataCollector = StubConnectedDeviceDataCollector( supportedDataTypes, deviceRegistration )
}
