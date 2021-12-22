package dk.cachet.carp.data.application

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.JsExport


/**
 * A collection of non-overlapping, ordered, data stream [sequences].
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
     * Determines whether this [DataStreamBatch] contains no [DataStreamPoint]s.
     */
    fun isEmpty(): Boolean = firstOrNull() == null

    /**
     * Get all [DataStreamPoint]s for [dataStream] in this batch, in order.
     */
    fun getDataStreamPoints( dataStream: DataStreamId ): Sequence<DataStreamPoint<*>> =
        sequences.filter { it.dataStream == dataStream }.flatMap { sequence -> sequence.map { it } }
}


/**
 * A mutable collection of non-overlapping data stream [sequences].
 */
@JsExport
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
     * @throws IllegalArgumentException when:
     *  - the start of the [sequence] range precedes the end of a previously appended sequence to the same data stream
     *  - the sync point of [sequence] is older than that of previous sequences in this batch
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
        require( last.syncPoint.synchronizedOn <= sequence.syncPoint.synchronizedOn )
            { "The sync point contained in this sequence can't have been obtained before a previous sync point." }

        // Merge sequence with last sequence if possible; add new sequence otherwise.
        if ( last.isImmediatelyFollowedBy( sequence ) )
        {
            last.appendSequence( sequence )
        }
        else { sequenceList.add( sequence.toMutableDataStreamSequence() ) }
    }

    /**
     * Append all data stream sequences contained in [batch] to this batch.
     *
     * @throws IllegalArgumentException when the start of any of the sequences contained in [batch]
     *   precede the end of a previously appended sequence to the same data stream.
     */
    fun appendBatch( batch: DataStreamBatch )
    {
        val containsNoPrecedingSequence = when ( batch )
        {
            is MutableDataStreamBatch ->
                // Preconditions for `MutableDataStreamBatch` can be verified much more easily.
                // This might seem like premature optimization, but currently it is the only concrete class.
                // We expect many sequences for one data type to be common, e.g., RR intervals have many sync points.
                batch.sequenceMap
                    .mapValues { it.value.last() }
                    .all { (dataStream, lastSequence) ->
                        val lastStoredSequence = sequenceMap[ dataStream ]?.last()
                        if ( lastStoredSequence == null ) true
                        else lastStoredSequence.range.last < lastSequence.range.first
                    }
            else ->
                batch.sequences.all { sequence ->
                    sequenceMap[ sequence.dataStream ]?.last().let { lastStoredSequence ->
                        if ( lastStoredSequence == null ) true
                        else lastStoredSequence.range.last < sequence.range.first
                    }
                }
        }
        require( containsNoPrecedingSequence )
            { "The batch contains a sequence of which the start precedes a previously appended sequence" }

        batch.sequences.forEach( ::appendSequence )
    }

    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( other !is DataStreamBatch ) return false

        return toList() == other.toList()
    }

    override fun hashCode(): Int = sequences.hashCode()
}


/**
 * Serializer [DataStreamBatch] as a list of [DataStreamSequence].
 */
@JsExport
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
        sequences.forEach( batch::appendSequence )

        return batch
    }
}
