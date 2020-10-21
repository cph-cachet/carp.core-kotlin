package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceType


/**
 * A [DeviceDataCollectorFactory] which for the local device data collector uses passed supported data types
 * and for connected devices tries to initialize [ConnectedDeviceDataCollector] for supported [DeviceType]
 * which are specified in [connectedSupportedDataTypes].
 *
 * @param localSupportedDataTypes The data types which are supported on the local device data collector.
 */
class StubConnectedDeviceDataCollectorFactory(
    localSupportedDataTypes: Set<DataType>,
    private val connectedSupportedDataTypes: Map<DeviceType, Set<DataType>>
) : DeviceDataCollectorFactory( StubDeviceDataCollector( localSupportedDataTypes ) )
{
    override fun createConnectedDataCollector(
        deviceType: DeviceType,
        deviceRegistration: DeviceRegistration
    ): ConnectedDeviceDataCollector
    {
        val supportedDataTypes = connectedSupportedDataTypes[ deviceType ]
            ?: throw UnsupportedOperationException( "No data collector for device of type `$deviceType` is available." )

        return StubConnectedDeviceDataCollector( supportedDataTypes, deviceRegistration )
    }
}
