package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.DataType


/**
 * Collects [Data] for a single device.
 *
 * TODO: Is 'start/stop' a suitable interface to control data collection?
 *       Implementations of this interface need to produce a stream of `Data` objects.
 *       Should this be one stream or individual streams per `DataType`?
 */
interface DeviceDataCollector
{
    /**
     * The set of [DataType]s defining which data can be collected on this device.
     */
    val supportedDataTypes: Set<DataType>

    /**
     * Start sampling data for the specified [dataTypes].
     * TODO: Does this correctly model measures which are self-terminating? Such as surveys?
     *
     * @throws UnsupportedOperationException when one of the passed [dataTypes] is not in [supportedDataTypes].
     */
    suspend fun start( dataTypes: Set<DataType> )

    /**
     * Stop sampling data for the specified [dataTypes].
     *
     * @throws UnsupportedOperationException when one of the passed [dataTypes] is not in [supportedDataTypes].
     */
    suspend fun stop( dataTypes: Set<DataType> )
}
