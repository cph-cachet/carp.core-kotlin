package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * A sequence of consecutive [measurements] for a [dataStream] starting from [firstSequenceId]
 * which all share the same [triggerIds] and [syncPoint].
 */
@Suppress( "DataClassPrivateConstructor" ) // Forcing initialization through `fromMeasurements` to clone lists.
@Serializable
data class DataStreamSequence<out TData : Data> private constructor(
    val dataStream: DataStreamId,
    val firstSequenceId: Long,
    val measurements: List<Measurement<TData>>,
    val triggerIds: List<Int>,
    val syncPoint: SyncPoint
)
{
    companion object
    {
        /**
         * Initialize [DataStreamSequence] from a list of [Measurement]s.
         */
        fun <TData : Data> fromMeasurements(
            dataStream: DataStreamId,
            firstSequenceId: Long,
            measurements: List<Measurement<TData>>,
            triggerIds: List<Int>,
            syncPoint: SyncPoint
        ) = DataStreamSequence( dataStream, firstSequenceId, measurements.toList(), triggerIds.toList(), syncPoint )
    }

    /**
     * The range of sequence IDs in the data stream covered by this [DataStreamSequence].
     */
    @Transient
    val range: LongRange = firstSequenceId until firstSequenceId + measurements.count()


    init
    {
        require( measurements.any() ) { "Sequence needs to contain at least one measurement." }
        require( firstSequenceId >= 0 ) { "Sequence ID must be positive." }

        // HACK: This is a workaround to circumvent a JS compilation bug: https://github.com/Kotlin/kotlinx.serialization/issues/247
        //  This can likely be replaced with the commented out code below once we upgrade to the IR backend.
        for ( measurement in measurements )
        {
            require( measurement.dataType == dataStream.dataType )
                { "Measurements in a sequence all need to correspond to the data type of the data stream." }
        }
        // require( measurements.all { it.dataType == dataStream.dataType } )
        //     { "Measurements in a batch all need to be of the same data type." }

        require( triggerIds.isNotEmpty() )
            { "Data always needs to be linked to at least one trigger that requested it." }
    }


    /**
     * Return [DataStreamPoint]s contained in this sequence.
     */
    fun getDataStreamPoints(): List<DataStreamPoint<TData>> =
        measurements.mapIndexed { index, measurement ->
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


/**
 * A serializer for [DataStreamSequence] to allow polymorphic serialization on the JS LEGACY backend.
 *
 * TODO: Verify whether this is still needed once we upgrade to the IR backend.
 */
object DataStreamSequenceSerializer : KSerializer<DataStreamSequence<Data>>
    by DataStreamSequence.serializer( PolymorphicSerializer( Data::class ) )
