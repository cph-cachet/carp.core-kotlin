package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * A batch of consecutive [measurements] starting from [firstSequenceId]
 * which all share the same data type, [triggerIds], and [syncPoint].
 */
@Serializable
data class DataStreamBatch<out TData : Data>(
    val firstSequenceId: Long,
    val measurements: List<Measurement<TData>>,
    val triggerIds: List<Int>,
    val syncPoint: SyncPoint
)
{
    @Transient
    val dataType: DataType = requireNotNull( measurements.map { it.dataType }.firstOrNull() )
        { "Batch needs to contain at least one measurement." }

    init
    {
        require( firstSequenceId >= 0 ) { "Sequence ID must be positive." }

        // HACK: This is a workaround to circumvent a JS compilation bug: https://github.com/Kotlin/kotlinx.serialization/issues/247
        //  This can likely be replaced with the commented out code below once we upgrade to the IR backend.
        for ( measurement in measurements )
        {
            require( measurement.dataType == dataType )
                { "Measurements in a batch all need to be of the same data type." }
        }
        // require( measurements.all { it.dataType == dataType } )
        //     { "Measurements in a batch all need to be of the same data type." }

        require( triggerIds.isNotEmpty() )
            { "Data always needs to be linked to at least one trigger that requested it." }
    }


    /**
     * Return [DataStreamPoint]s contained in this batch marked as belonging to [dataStream].
     */
    fun getDataStreamPoints( dataStream: DataStreamId ): List<DataStreamPoint<TData>>
    {
        require( dataType == dataStream.dataType )
            { "Measurements data type does not correspond to the data stream data type." }

        return measurements.mapIndexed { index, measurement ->
            DataStreamPoint(
                firstSequenceId + index,
                dataStream.studyDeploymentId,
                dataStream.deviceRoleName,
                measurement,
                triggerIds,
                syncPoint
            )
        }
    }
}


/**
 * A serializer for [DataStreamBatch] to allow polymorphic serialization on the JS LEGACY backend.
 *
 * TODO: Verify whether this is still needed once we upgrade to the IR backend.
 */
object DataStreamBatchSerializer : KSerializer<DataStreamBatch<Data>>
    by DataStreamBatch.serializer( PolymorphicSerializer( Data::class ) )

