package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.devices.DeviceDescriptor


/**
 * A helper class to construct iterable objects which list all available tasks for a [DeviceDescriptor].
 * All devices support [BackgroundTask], which is added by default as [BACKGROUND].
 *
 * Extend from this class as an object and assign members as follows: `val SOME_TASK = add { SomeTaskBuilder() }`.
 */
open class TaskDescriptorList private constructor( private val list: MutableList<SupportedTaskDescriptor<*, *>> ) :
    List<SupportedTaskDescriptor<*, *>> by list
{
    constructor() : this( mutableListOf() )

    /**
     * All containing measures and/or outputs start running in the background once triggered.
     * The task runs for a specified duration, or until stopped, or until all measures and/or outputs have completed.
     */
    @Suppress("PropertyName", "VariableNaming" ) // This class should only be extended by object classes, making it a constant.
    val BACKGROUND = add { BackgroundTaskBuilder() }


    protected fun <TTaskDescriptor : TaskDescriptor, TBuilder : TaskDescriptorBuilder<TTaskDescriptor>> add(
        builder: () -> TBuilder
    ): SupportedTaskDescriptor<TTaskDescriptor, TBuilder> = SupportedTaskDescriptor( builder ).also { list.add( it ) }
}


/**
 * A [TaskDescriptor] which is listed as a supported task on a [DeviceDescriptor].
 */
class SupportedTaskDescriptor<TTaskDescriptor : TaskDescriptor, TBuilder : TaskDescriptorBuilder<TTaskDescriptor>>(
    private val createBuilder: () -> TBuilder
)
{
    /**
     * Create a [TaskDescriptor] supported on this device with [name] which uniquely defines the task
     * and [description] which explains why the data is collected.
     */
    fun create( name: String, builder: TBuilder.() -> Unit ): TTaskDescriptor =
        createBuilder()
            .apply( builder )
            .build( name )
}
