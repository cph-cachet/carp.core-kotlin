package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.application.data.DataType


class StubDeviceDataCollector( override val supportedDataTypes: Set<DataType> ) : DeviceDataCollector
