package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceType


/**
 * Allows subscribing to [Data] of requested [DataType]s on a master device and connected devices
 * by using [DeviceDataCollector] instances provided by [dataCollectorFactory].
 */
class DataListener( private val dataCollectorFactory: DeviceDataCollectorFactory )
{
    private val connectedDataCollectors: MutableMap<DeviceType, MutableMap<DeviceRegistration, ConnectedDeviceDataCollector>> = mutableMapOf()


    /**
     * Determines whether subscribing to [Data] of a given [dataType] is supported on this master device.
     */
    fun supportsData( dataType: DataType ): Boolean =
        dataType in dataCollectorFactory.localDataCollector.supportedDataTypes

    /**
     * Determines whether subscribing to [Data] of a given [dataType] is supported
     * by connecting to a device of type [connectedDeviceType] using [deviceRegistration].
     */
    fun supportsDataOnConnectedDevice(
        dataType: DataType,
        connectedDeviceType: DeviceType,
        deviceRegistration: DeviceRegistration
    ): Boolean
    {
        return try
        {
            val dataCollector = connectedDataCollectors
                .getOrPut( connectedDeviceType, { mutableMapOf() } )
                .getOrPut(
                    deviceRegistration,
                    { dataCollectorFactory.createConnectedDataCollector( connectedDeviceType, deviceRegistration ) }
                )
            dataType in dataCollector.supportedDataTypes
        }
        catch ( ex: UnsupportedOperationException ) { false } // In case `createConnectedDataCollector` fails.
    }
}
