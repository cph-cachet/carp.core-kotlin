package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import dk.cachet.carp.common.serialization.createUnknownPolymorphicSerializer
import dk.cachet.carp.protocols.domain.tasks.measures.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.ArrayListSerializer


/**
 * Custom serializer for a list of [Measure]s which enables deserializing types that are unknown at runtime, yet extend from [Measure].
 */
object MeasuresSerializer : KSerializer<List<Measure>> by ArrayListSerializer<Measure>(
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
    @Transient
    abstract val name: String

    /**
     * The data which needs to be collected/measured as part of this task.
     */
    @Transient
    abstract val measures: List<Measure>
}