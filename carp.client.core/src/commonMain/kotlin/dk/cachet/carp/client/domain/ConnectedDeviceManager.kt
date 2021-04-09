package dk.cachet.carp.client.domain

import dk.cachet.carp.client.domain.data.AnyConnectedDeviceDataCollector
import dk.cachet.carp.common.application.devices.DeviceRegistration


/**
 * Provides access to the status of a device which is connected to a master device.
 *
 * TODO: Through here a reactive stream of device state (connectivity/battery) should be exposed, and last known state can be stored.
 */
class ConnectedDeviceManager(
    val deviceRegistration: DeviceRegistration,
    // TODO: This should not be public here.
    //       Currently, it is as a first MVP which starts with creating data collectors as determined by the domain model.
    val dataCollector: AnyConnectedDeviceDataCollector
)
{
    /**
     * Determines whether a connection can be made at this point in time to the device.
     */
    suspend fun canConnect(): Boolean = dataCollector.canConnect()
}
