package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.common.Immutable
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import dk.cachet.carp.protocols.domain.serialization.createUnknownPolymorphicSerializer
import kotlinx.serialization.*
import kotlinx.serialization.internal.ArrayListSerializer


internal object MeasuresSerializer : KSerializer<List<Measure>> by ArrayListSerializer<Measure>(
    createUnknownPolymorphicSerializer { className, json -> CustomMeasure( className, json ) }
)


/**
 * Describes requested measures and/or output to be presented on a device.
 * TODO: Outputs are not yet specified.
 */
@Serializable
abstract class TaskDescriptor : Immutable( notImmutableErrorFor( TaskDescriptor::class ) )
{
    /**
     * A name which uniquely identifies the task.
     */
    abstract val name: String

    /**
     * The data which needs to be collected/measured as part of this task.
     */
    @Serializable( with = MeasuresSerializer::class )
    abstract val measures: List<Measure>
}