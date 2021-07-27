package dk.cachet.carp.data.application

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * A sequence of consecutive [measurements] for a [dataStream] starting from [firstSequenceId]
 * which all share the same [triggerIds] and [syncPoint].
 */
@Serializable( DataStreamSequenceSerializer::class )
class DataStreamSequence(
    val dataStream: DataStreamId,
    val firstSequenceId: Long,
    triggerIds: List<Int>,
    val syncPoint: SyncPoint
)
{
    init
    {
        require( firstSequenceId >= 0 ) { "Sequence ID must be positive." }
        require( triggerIds.isNotEmpty() )
            { "Data always needs to be linked to at least one trigger that requested it." }
    }

    val triggerIds = triggerIds.toList()

    private val _measurements: MutableList<Measurement<*>> = mutableListOf()
    val measurements: List<Measurement<*>>
        get() = _measurements

    /**
     * The current range of sequence IDs in the data stream covered by this [DataStreamSequence].
     */
    val range: LongRange
        get() =
            if ( _measurements.size == 0 ) LongRange.EMPTY
            else firstSequenceId until firstSequenceId + _measurements.size


    /**
     * Append [measurements] to the end of this sequence.
     *
     * @throws IllegalArgumentException when any of the [measurements] is of a different data type than [dataStream].
     */
    fun appendMeasurements( measurements: List<Measurement<*>> )
    {
        require( measurements.all { it.dataType == dataStream.dataType } )
            { "Measurements all need to correspond to the data type of the data stream." }

        _measurements.addAll( measurements )
    }

    /**
     * Append [measurements] to the end of this sequence.
     *
     * @throws IllegalArgumentException when any of the [measurements] is of a different data type than [dataStream].
     */
    fun appendMeasurements( vararg measurements: Measurement<*> ) = appendMeasurements( measurements.toList() )

    /**
     * Append all measurements of [sequence] to this sequence.
     *
     * @throws IllegalArgumentException when [sequence] doesn't match this sequence or doesn't immediately follow
     *   the last data point in this sequence.
     */
    fun appendSequence( sequence: DataStreamSequence )
    {
        require( canAppendSequence( sequence ) )
            { "Sequence doesn't match or doesn't immediately follow the last data point." }

        appendMeasurements( sequence.measurements )
    }

    /**
     * Determines whether [sequence] is a sequence for the same data stream with matching [triggerIds] and [syncPoint]
     * and immediately follows the last data point in this sequence.
     */
    fun canAppendSequence( sequence: DataStreamSequence ): Boolean =
        dataStream == sequence.dataStream &&
        triggerIds == sequence.triggerIds &&
        syncPoint == sequence.syncPoint &&
        if ( range == LongRange.EMPTY ) firstSequenceId == sequence.firstSequenceId
        else range.last + 1 == sequence.firstSequenceId

    /**
     * Return [DataStreamPoint]s contained in this sequence.
     */
    fun getDataStreamPoints(): List<DataStreamPoint<*>> =
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


internal object DataStreamSequenceSerializer : KSerializer<DataStreamSequence>
{
    @Serializable
    class DataStreamSequenceSurrogate(
        val dataStream: DataStreamId,
        val firstSequenceId: Long,
        val measurements: List<Measurement<*>>,
        val triggerIds: List<Int>,
        val syncPoint: SyncPoint
    )

    private val serializer = DataStreamSequenceSurrogate.serializer()
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize( encoder: Encoder, value: DataStreamSequence ) =
        encoder.encodeSerializableValue(
            serializer,
            DataStreamSequenceSurrogate(
                value.dataStream,
                value.firstSequenceId,
                value.measurements,
                value.triggerIds,
                value.syncPoint
            )
        )

    override fun deserialize( decoder: Decoder ): DataStreamSequence
    {
        val surrogate = decoder.decodeSerializableValue( serializer )
        val sequence = DataStreamSequence(
            surrogate.dataStream,
            surrogate.firstSequenceId,
            surrogate.triggerIds,
            surrogate.syncPoint
        )
        sequence.appendMeasurements( surrogate.measurements )

        return sequence
    }
}
