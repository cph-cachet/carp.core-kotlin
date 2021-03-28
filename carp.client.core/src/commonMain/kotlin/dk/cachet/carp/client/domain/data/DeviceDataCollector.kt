package dk.cachet.carp.client.domain.data

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataType


/**
 * Collects [Data] for a single device.
 */
interface DeviceDataCollector
{
    /**
     * The set of [DataType]s defining which data can be collected on this device.
     */
    val supportedDataTypes: Set<DataType>
}
