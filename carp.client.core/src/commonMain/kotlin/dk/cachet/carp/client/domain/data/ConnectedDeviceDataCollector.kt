package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Collects [Data] for a single external device.
 */
abstract class ConnectedDeviceDataCollector( val registration: DeviceRegistration ) : DeviceDataCollector
{
    /**
     * Determines whether a connection can be made at this point in time to the device.
     */
    abstract suspend fun canConnect(): Boolean
}
