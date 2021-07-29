package dk.cachet.carp.data.application

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * A collection of non-overlapping data stream [sequences].
 */
interface DataStreamBatch : Sequence<DataStreamPoint<*>>
{
    val sequences: Sequence<DataStreamSequence>


    /**
     * Get an iterator to iterate over all [DataStreamPoint]s contained in this batch.
     */
    override fun iterator(): Iterator<DataStreamPoint<*>> =
        sequences.asSequence().flatMap { seq -> seq.map { it } }.iterator()


    /**
     * A [DataStreamBatch] with no data points.
     */
    object EMPTY : DataStreamBatch
    {
        override val sequences: Sequence<DataStreamSequence> = emptySequence()
    }
}


/**
 * A mutable collection of non-overlapping data stream [sequences].
 */
class MutableDataStreamBatch : DataStreamBatch
{
    private val sequenceMap: MutableMap<DataStreamId, MutableList<MutableDataStreamSequence>> = mutableMapOf()

    /**
     * A list of sequences covering all sequences so far appended to this [MutableDataStreamBatch].
     * This may return less sequences than originally appended in case appended sequences were merged with prior ones.
     */
    override val sequences: Sequence<DataStreamSequence>
        get() = sequenceMap.asSequence().flatMap { it.value }


    /**
     * Append a sequence to a non-existing or previously appended data stream in this batch.
     *
     * @throws IllegalArgumentException when the start of the [sequence] range precedes the end of
     *   a previously appended sequence to the same data stream.
     */
    fun appendSequence( sequence: DataStreamSequence )
    {
        val sequenceList = sequenceMap[ sequence.dataStream ]

        // Early out if this is the first sequence added for this data stream.
        if ( sequenceList == null )
        {
            sequenceMap[ sequence.dataStream ] = mutableListOf( sequence.toMutableDataStreamSequence() )
            return
        }

        val last = sequenceList.last()
        require( last.range.last < sequence.range.first )
            { "Sequence range start lies before the end of a previously appended sequence to the same data stream." }

        // Merge sequence with last sequence if possible; add new sequence otherwise.
        if ( last.isImmediatelyFollowedBy( sequence ) )
        {
            last.appendSequence( sequence )
        }
        else { sequenceList.add( sequence.toMutableDataStreamSequence() ) }
    }
}


/**
 * Serializer [DataStreamBatch] as a list of [DataStreamSequence].
 */
object DataStreamBatchSerializer : KSerializer<DataStreamBatch>
{
    private val serializer = ListSerializer( DataStreamSequenceSerializer )
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize( encoder: Encoder, value: DataStreamBatch ) =
        encoder.encodeSerializableValue( serializer, value.sequences.toList() )

    override fun deserialize( decoder: Decoder ): DataStreamBatch
    {
        val batch = MutableDataStreamBatch()

        val sequences = decoder.decodeSerializableValue( serializer )
        sequences.forEach { batch.appendSequence( it ) }

        return batch
    }
}
