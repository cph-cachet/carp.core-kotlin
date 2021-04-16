package dk.cachet.carp.clients.domain.data

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceType


/**
 * Allows subscribing to [Data] (e.g., sensors, surveys) of requested [DataType]s on a master device and connected devices
 * by using [DeviceDataCollector] instances provided by [dataCollectorFactory].
 */
class DataListener( private val dataCollectorFactory: DeviceDataCollectorFactory )
{
    private val connectedDataCollectors: MutableMap<DeviceType, MutableMap<DeviceRegistration, AnyConnectedDeviceDataCollector>> = mutableMapOf()


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
    ): Boolean =
        tryGetConnectedDataCollector( connectedDeviceType, deviceRegistration )
            ?.let { dataType in it.supportedDataTypes }
            ?: false

    /**
     * Retrieve existing [ConnectedDeviceDataCollector] for the given [connectedDeviceType] and [registration],
     * or try to create a new one in case it was not created before.
     *
     * @return The [ConnectedDeviceDataCollector], or null in case it could not be created.
     */
    fun tryGetConnectedDataCollector( connectedDeviceType: DeviceType, registration: DeviceRegistration ): AnyConnectedDeviceDataCollector?
    {
        return try
        {
            connectedDataCollectors
                .getOrPut( connectedDeviceType, { mutableMapOf() } )
                .getOrPut(
                    registration,
                    { dataCollectorFactory.createConnectedDataCollector( connectedDeviceType, registration ) }
                )
        }
        catch ( _: UnsupportedOperationException ) { null }
    }
}
