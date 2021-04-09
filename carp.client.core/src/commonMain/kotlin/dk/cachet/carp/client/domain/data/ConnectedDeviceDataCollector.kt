package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.devices.DeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistrationBuilder


/**
 * Collects [Data] for a single external device.
 */
abstract class ConnectedDeviceDataCollector<
    TDeviceDescriptor : DeviceDescriptor<TRegistration, DeviceRegistrationBuilder<TRegistration>>,
    TRegistration : DeviceRegistration
>( val registration: TRegistration ) : DeviceDataCollector
{
    /**
     * Determines whether a connection can be made at this point in time to the device.
     */
    abstract suspend fun canConnect(): Boolean
}

typealias AnyConnectedDeviceDataCollector = ConnectedDeviceDataCollector<*, *>
