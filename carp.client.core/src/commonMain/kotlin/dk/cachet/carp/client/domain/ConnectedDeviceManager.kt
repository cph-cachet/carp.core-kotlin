package dk.cachet.carp.client.domain

import dk.cachet.carp.client.domain.data.AnyConnectedDeviceDataCollector
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Provides access to the status of a device which is connected to a master device.
 *
 * TODO: Through here a reactive stream of device state (connectivity/battery) should be exposed, and last known state can be stored.
 */
class ConnectedDeviceManager(
    val deviceRegistration: DeviceRegistration,
    private val dataCollector: AnyConnectedDeviceDataCollector
)
{
    /**
     * Determines whether a connection can be made at this point in time to the device.
     */
    suspend fun canConnect(): Boolean = dataCollector.canConnect()
}
