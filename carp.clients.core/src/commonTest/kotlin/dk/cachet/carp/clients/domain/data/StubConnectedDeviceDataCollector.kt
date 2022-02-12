package dk.cachet.carp.clients.domain.data

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistrationBuilder


class StubConnectedDeviceDataCollector<
    TDeviceConfiguration : DeviceConfiguration<TRegistration, DeviceRegistrationBuilder<TRegistration>>,
    TRegistration : DeviceRegistration
>(
    override val supportedDataTypes: Set<DataType>,
    registration: TRegistration
) : ConnectedDeviceDataCollector<TDeviceConfiguration, TRegistration>( registration )
{
    override suspend fun canConnect(): Boolean = true
}
