package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import kotlinx.serialization.Serializable


/**
 * A [TaskDescriptor] which specifies that all containing measures and/or outputs should start immediately once triggered and run indefinitely.
 */
@Serializable
data class IndefiniteTask(
    override val name: String,
    @Serializable( MeasuresSerializer::class )
    override val measures: List<Measure> ) : TaskDescriptor()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( IndefiniteTask::class, "dk.cachet.carp.protocols.domain.tasks.IndefiniteTask" ) }
    }
}