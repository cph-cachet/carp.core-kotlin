package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Collects [Data] for a single external device.
 *
 * TODO: Include battery and connectivity information.
 */
abstract class ConnectedDeviceDataCollector( val registration: DeviceRegistration ) : DeviceDataCollector
