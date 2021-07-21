package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * A [measurement] as part of a [dataStream] defined in an associated deployed study protocol
 * which was collected because the triggers identified by [triggerIds] requested data collection.
 */
@Serializable
data class DataStreamPoint<out TData : Data>(
    /**
     * The sequence number of this [measurement] in the data stream, acting as a unique key.
     * Sequence numbers are assigned by the master device which coordinates the data collection,
     * starting from zero and incremented by 1 for each new measurement.
     */
    val sequenceId: Long,
    val studyDeploymentId: UUID,
    val deviceRoleName: String,
    @Serializable( MeasurementSerializer::class )
    val measurement: Measurement<TData>,
    val triggerIds: List<Int>,
    /**
     * The most recent synchronization information which was determined for this or a previous [DataStreamPoint].
     */
    val syncPoint: SyncPoint
)
{
    init
    {
        require( sequenceId >= 0 ) { "Sequence ID must be positive." }
        require( triggerIds.isNotEmpty() )
            { "Any data always needs to be linked back to at least one trigger that requested it." }
    }

    val dataStream: DataStreamId
        get() = DataStreamId( studyDeploymentId, deviceRoleName, measurement.dataType )
}


/**
 * A serializer for [DataStreamPoint] to allow polymorphic serialization on the JS LEGACY backend.
 *
 * TODO: Verify whether this is still needed once we upgrade to the IR backend.
 */
object DataStreamPointSerializer : KSerializer<DataStreamPoint<Data>>
    by DataStreamPoint.serializer( PolymorphicSerializer( Data::class ) )
