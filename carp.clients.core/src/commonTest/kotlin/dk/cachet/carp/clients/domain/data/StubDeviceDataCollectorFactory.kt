package dk.cachet.carp.clients.domain.data

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceType


/**
 * A [DeviceDataCollectorFactory] which for the local device data collector uses passed supported data types
 * and for connected devices uses [connectedSupportedDataTypes].
 *
 * @param localSupportedDataTypes The data types which are supported on the local device data collector.
 */
class StubDeviceDataCollectorFactory(
    localSupportedDataTypes: Set<DataType>,
    private val connectedSupportedDataTypes: Set<DataType>
) : DeviceDataCollectorFactory( StubDeviceDataCollector( localSupportedDataTypes ) )
{
    override fun createConnectedDataCollector(
        deviceType: DeviceType,
        deviceRegistration: DeviceRegistration
    ): AnyConnectedDeviceDataCollector =
        StubConnectedDeviceDataCollector( connectedSupportedDataTypes, deviceRegistration )
}
