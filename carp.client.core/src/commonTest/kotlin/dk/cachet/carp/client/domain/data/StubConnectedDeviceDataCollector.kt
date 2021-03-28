package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistrationBuilder


class StubConnectedDeviceDataCollector<
    TDeviceDescriptor : DeviceDescriptor<TRegistration, DeviceRegistrationBuilder<TRegistration>>,
    TRegistration : DeviceRegistration
>(
    override val supportedDataTypes: Set<DataType>,
    registration: TRegistration
) : ConnectedDeviceDataCollector<TDeviceDescriptor, TRegistration>( registration )
{
    override suspend fun canConnect(): Boolean = true
}
