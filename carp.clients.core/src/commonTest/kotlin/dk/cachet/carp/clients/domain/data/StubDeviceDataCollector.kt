package dk.cachet.carp.clients.domain.data

import dk.cachet.carp.common.application.data.DataType


class StubDeviceDataCollector( override val supportedDataTypes: Set<DataType> ) : DeviceDataCollector
