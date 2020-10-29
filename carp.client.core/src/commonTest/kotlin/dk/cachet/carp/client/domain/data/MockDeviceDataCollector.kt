package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.test.Mock


class MockDeviceDataCollector( override val supportedDataTypes: Set<DataType> ) : Mock<DeviceDataCollector>(), DeviceDataCollector
{
    override suspend fun start( dataTypes: Set<DataType> ) = trackSuspendCall( DeviceDataCollector::start, dataTypes )

    override suspend fun stop( dataTypes: Set<DataType> ) = trackSuspendCall( DeviceDataCollector::stop, dataTypes )
}
