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
sealed interface DataStreamSequence : Sequence<DataStreamPoint<*>>
{
    val dataStream: DataStreamId
    val firstSequenceId: Long
    val measurements: List<Measurement<*>>
    val triggerIds: List<Int>
    val syncPoint: SyncPoint

    /**
     * The current range of sequence IDs in the data stream covered by this [DataStreamSequence].
     */
    val range: LongRange get() =
        if ( measurements.isEmpty() ) LongRange.EMPTY
        else firstSequenceId until firstSequenceId + measurements.size


    /**
     * @throws IllegalStateException when the current state of this [DataStreamSequence] violates interface constraints.
     */
    fun throwIfIllegalState()
    {
        check( firstSequenceId >= 0 ) { "Sequence ID must be positive." }
        check( triggerIds.isNotEmpty() )
            { "Data always needs to be linked to at least one trigger that requested it." }
        check( measurements.all { it.dataType == dataStream.dataType } )
            { "Measurements all need to correspond to the data type of the data stream." }
    }

    /**
     * Get an iterator to iterate over [DataStreamPoint]s contained in this sequence.
     */
    override fun iterator(): Iterator<DataStreamPoint<*>> =
        measurements.asSequence().mapIndexed { index, measurement ->
            DataStreamPoint(
                firstSequenceId + index,
                dataStream.studyDeploymentId,
                dataStream.deviceRoleName,
                measurement,
                triggerIds,
                syncPoint
            )
        }.iterator()

    /**
     * Determines whether [sequence] is a sequence for the same data stream with matching [triggerIds] and [syncPoint]
     * and immediately follows the last data point in this sequence.
     */
    fun isImmediatelyFollowedBy( sequence: DataStreamSequence ): Boolean =
        dataStream == sequence.dataStream &&
        triggerIds == sequence.triggerIds &&
        syncPoint == sequence.syncPoint &&
        if ( range == LongRange.EMPTY ) firstSequenceId == sequence.firstSequenceId
        else range.last + 1 == sequence.firstSequenceId


    /**
     * Returns a new [MutableDataStreamSequence], containing all the measurements of this sequence.
     */
    fun toMutableDataStreamSequence(): MutableDataStreamSequence
    {
        val sequence = MutableDataStreamSequence(
            dataStream,
            firstSequenceId,
            triggerIds,
            syncPoint
        )
        sequence.appendMeasurements( measurements )

        return sequence
    }
}


/**
 * A mutable sequence of consecutive [measurements] for a [dataStream] starting from [firstSequenceId]
 * which all share the same [triggerIds] and [syncPoint].
 */
class MutableDataStreamSequence(
    override val dataStream: DataStreamId,
    override val firstSequenceId: Long,
    triggerIds: List<Int>,
    override val syncPoint: SyncPoint
) : DataStreamSequence
{
    override val triggerIds: List<Int> = triggerIds.toList()

    private val _measurements: MutableList<Measurement<*>> = mutableListOf()
    override val measurements: List<Measurement<*>>
        get() = _measurements

    init { throwIfIllegalInitialization() }


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
        require( isImmediatelyFollowedBy( sequence ) )
            { "Sequence doesn't match or doesn't immediately follow the last data point." }

        appendMeasurements( sequence.measurements )
    }
}


/**
 * Serializer for any [DataStreamSequence], which doesn't guarantee the concrete type is retained.
 */
object DataStreamSequenceSerializer : KSerializer<DataStreamSequence>
{
    @Serializable
    class DataStreamSequenceSnapshot internal constructor(
        override val dataStream: DataStreamId,
        override val firstSequenceId: Long,
        override val measurements: List<Measurement<*>>,
        override val triggerIds: List<Int>,
        override val syncPoint: SyncPoint
    ) : DataStreamSequence
    {
        init { throwIfIllegalInitialization() }
    }

    private val serializer = DataStreamSequenceSnapshot.serializer()
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize( encoder: Encoder, value: DataStreamSequence ) =
        encoder.encodeSerializableValue(
            serializer,
            DataStreamSequenceSnapshot(
                value.dataStream,
                value.firstSequenceId,
                value.measurements,
                value.triggerIds,
                value.syncPoint
            )
        )

    override fun deserialize( decoder: Decoder ): DataStreamSequence = decoder.decodeSerializableValue( serializer )
}

private fun DataStreamSequence.throwIfIllegalInitialization() =
    try { throwIfIllegalState() }
    catch ( ex: IllegalStateException ) { throw IllegalArgumentException( ex ) }
