package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import dk.cachet.carp.protocols.domain.serialization.*


/**
 * A [TaskDescriptor] which specifies that all containing measures and/or outputs should start immediately once triggered and run indefinitely.
 */
@Serializable
data class IndefiniteTask(
    override val name: String,
    @SerializableWith( MeasuresSerializer::class )
    override val measures: List<Measure> ) : TaskDescriptor()