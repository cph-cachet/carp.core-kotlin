package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import kotlinx.serialization.Polymorphic


/**
 * Describes requested measures and/or output to be presented on a device.
 * TODO: Outputs are not yet specified.
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
interface TaskDescriptor
{
    /**
     * A name which uniquely identifies the task.
     */
    val name: String

    /**
     * The data which needs to be collected/measured passively as part of this task.
     */
    val measures: List<Measure>

    /**
     * A description of this task, emphasizing the reason why the data is collected.
     */
    val description: String?
}


/**
 * A helper class to configure and construct immutable [TaskDescriptor] classes.
 */
@TaskDescriptorBuilderDsl
abstract class TaskDescriptorBuilder<TTaskDescriptor : TaskDescriptor>
{
    /**
     * The data which needs to be collected/measures as part of this task.
     */
    var measures: List<Measure> = emptyList()

    /**
     * A description of this task, emphasizing the reason why the data is collected.
     */
    var description: String? = null

    abstract fun build( name: String ): TTaskDescriptor
}

/**
 * Should be applied to all builders participating in building [TaskDescriptor]s to prevent misuse of internal DSL.
 * For more information: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-dsl-marker/index.html
 */
@DslMarker
annotation class TaskDescriptorBuilderDsl
