package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.DataType
import kotlinx.serialization.Serializable


/**
 * A [Measure] which is defined by nothing else but the [DataType] identifier.
 * It is up to the 'client' (e.g., smartphone runtime) to determine how to handle this measure.
 */
@Serializable
data class DataTypeMeasure( override val type: DataType ) : Measure()
{
    /**
     * Create a new [DataTypeMeasure] for a specific [DataType] by specifying the [namespace] and [name] of the [DataType] directly.
     * This is an alternate constructor for mere convenience.
     */
    constructor(
        /**
         * The namespace of the [DataType] for which data needs to be measured.
         * Uniquely identifies the organization/person who determines how to interpret [name].
         */
        namespace: String,
        /**
         * The name of the [DataType] for which data needs to be measured.
         * Describes the data being collected.
         */
        name: String
    ) : this( DataType( namespace, name ) )
}
