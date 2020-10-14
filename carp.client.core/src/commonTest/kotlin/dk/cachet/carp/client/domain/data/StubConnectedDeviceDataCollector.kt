package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


class StubConnectedDeviceDataCollector(
    override val supportedDataTypes: Set<DataType>,
    registration: DeviceRegistration
) : ConnectedDeviceDataCollector( registration )
