package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.devices.DeviceConfiguration


/**
 * A helper class to construct iterable objects which list all available tasks for a [DeviceConfiguration].
 * All devices support [BackgroundTask], which is added by default as [BACKGROUND].
 *
 * Extend from this class as an object and assign members as follows: `val SOME_TASK = add { SomeTaskBuilder() }`.
 */
open class TaskConfigurationList private constructor( private val list: MutableList<SupportedTaskConfiguration<*, *>> ) :
    List<SupportedTaskConfiguration<*, *>> by list
{
    constructor() : this( mutableListOf() )

    /**
     * All containing measures and/or outputs start running in the background once triggered.
     * The task runs for a specified duration, or until stopped, or until all measures and/or outputs have completed.
     */
    @Suppress("PropertyName", "VariableNaming" ) // This class should only be extended by object classes, making it a constant.
    val BACKGROUND = add { BackgroundTaskBuilder() }


    protected fun <TConfiguration : TaskConfiguration<*>, TBuilder : TaskConfigurationBuilder<TConfiguration>> add(
        builder: () -> TBuilder
    ): SupportedTaskConfiguration<TConfiguration, TBuilder> = SupportedTaskConfiguration( builder ).also { list.add( it ) }
}


/**
 * A [TaskConfiguration] which is listed as a supported task on a [DeviceConfiguration].
 */
class SupportedTaskConfiguration<TConfiguration : TaskConfiguration<*>, TBuilder : TaskConfigurationBuilder<TConfiguration>>(
    private val createBuilder: () -> TBuilder
)
{
    /**
     * Create a [TaskConfiguration] supported on this device with [name] which uniquely defines the task.
     */
    fun create( name: String, builder: TBuilder.() -> Unit ): TConfiguration =
        createBuilder()
            .apply( builder )
            .build( name )
}
