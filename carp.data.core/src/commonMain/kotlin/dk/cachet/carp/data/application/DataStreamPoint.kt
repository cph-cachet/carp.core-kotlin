package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.Serializable


/**
 * A [measurement] as part of a [dataStream] defined in an associated deployed study protocol
 * which was collected because the triggers identified by [triggerIds] requested data collection.
 */
@Serializable
data class DataStreamPoint<TData : Data>(
    /**
     * The sequence number of this [measurement] in the data stream, acting as a unique key.
     * Sequence numbers are assigned by the master device which coordinates the data collection,
     * starting from zero and incremented by 1 for each new measurement.
     */
    val sequenceId: Long,
    val studyDeploymentId: UUID,
    val deviceRoleName: String,
    val measurement: Measurement<TData>,
    val triggerIds: List<Int>,
    /**
     * The most recent synchronization information which was determined for this or a previous [DataStreamPoint].
     */
    val syncPoint: SyncPoint
)
{
    val dataStream: DataStreamId
        get() = DataStreamId( studyDeploymentId, deviceRoleName, measurement.dataType )
}
