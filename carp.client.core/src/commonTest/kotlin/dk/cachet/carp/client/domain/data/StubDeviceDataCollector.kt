package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.DataType


class StubDeviceDataCollector( override val supportedDataTypes: Set<DataType> ) : DeviceDataCollector
{
    override suspend fun start( dataTypes: Set<DataType> )
    {
        // Nothing to do.
    }
    override suspend fun stop( dataTypes: Set<DataType> )
    {
        // Nothing to do.
    }
}
