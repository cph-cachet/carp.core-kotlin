package dk.cachet.carp.data.application

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * A collection of non-overlapping [DataStreamSequence]s.
 */
@Serializable( DataStreamBatchSerializer::class )
class DataStreamBatch
{
    private val sequenceMap: MutableMap<DataStreamId, MutableList<DataStreamSequence<*>>> = mutableMapOf()

    /**
     * A list of sequences covering all sequences so far appended to this [DataStreamBatch].
     * This may return less sequences than originally appended in case appended sequences were merged with prior ones.
     */
    val sequences: List<DataStreamSequence<*>>
        get() = sequenceMap.flatMap { it.value }


    /**
     * Append a sequence to a non-existing or previously appended data stream in this batch.
     *
     * @throws IllegalArgumentException when the start of the [sequence] range precedes the end of
     *   a previously appended sequence to the same data stream.
     */
    fun appendSequence( sequence: DataStreamSequence<*> )
    {
        val sequences = sequenceMap[ sequence.dataStream ]

        // Early out if this is the first sequence added for this data stream.
        if ( sequences == null )
        {
            sequenceMap[ sequence.dataStream ] = mutableListOf( sequence )
            return
        }

        val last = sequences.last()
        require( last.range.last < sequence.range.first )
            { "Sequence range start lies before the end of a previously appended sequence to the same data stream." }

        // Merge sequence with last sequence if possible; append otherwise.
        // TODO: Some logic can likely be refactored to be part of `DataStreamSequence`.
        val metaDataMatches = last.triggerIds == sequence.triggerIds && last.syncPoint == sequence.syncPoint
        val followsSequence = last.range.last + 1 == sequence.firstSequenceId
        if ( metaDataMatches && followsSequence )
        {
            sequences.removeLast()
            sequences.add(
                DataStreamSequence.fromMeasurements(
                    sequence.dataStream,
                    last.firstSequenceId,
                    last.measurements.plus( sequence.measurements ),
                    sequence.triggerIds,
                    sequence.syncPoint
                )
            )
        }
        else { sequences.add( sequence ) }
    }

    /**
     * Return all [DataStreamPoint]s contained in this sequence.
     */
    fun getDataStreamPoints(): List<DataStreamPoint<*>> = sequences.flatMap { it.getDataStreamPoints() }
}


/**
 * A custom serializer for [DataStreamBatch] which serializes the list of sequences contained within.
 */
class DataStreamBatchSerializer : KSerializer<DataStreamBatch>
{
    private val serializer = ListSerializer( DataStreamSequenceSerializer )
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize( encoder: Encoder, value: DataStreamBatch ) =
        encoder.encodeSerializableValue( serializer, value.sequences )

    override fun deserialize( decoder: Decoder ): DataStreamBatch
    {
        val batch = DataStreamBatch()

        val sequences = decoder.decodeSerializableValue( serializer )
        sequences.forEach { batch.appendSequence( it ) }

        return batch
    }
}
