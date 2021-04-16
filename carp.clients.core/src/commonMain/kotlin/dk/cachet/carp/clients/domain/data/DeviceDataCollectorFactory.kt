package dk.cachet.carp.clients.domain.data

import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceType


/**
 * Provides a [localDataCollector] to collect data locally on the master device
 * and supports creating [ConnectedDeviceDataCollector] instances for connected devices.
 */
abstract class DeviceDataCollectorFactory( val localDataCollector: DeviceDataCollector )
{
    /**
     * Create a [ConnectedDeviceDataCollector] for a connected [deviceType]
     * using connection options specified in [deviceRegistration].
     *
     * @throws UnsupportedOperationException in case the [ConnectedDeviceDataCollector] cannot be created.
     */
    abstract fun createConnectedDataCollector(
        deviceType: DeviceType,
        deviceRegistration: DeviceRegistration
    ): AnyConnectedDeviceDataCollector
}
