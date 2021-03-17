package dk.cachet.carp.common.ddd

import kotlin.reflect.KClass


/**
 * An event bus which can be used by an [ApplicationService] to publish events associated with its service
 * and subscribe to any other events.
 */
class ApplicationServiceEventBus<
    TService : ApplicationService<TService, TEvent>,
    TEvent : IntegrationEvent<TService>
>( private val serviceKlass: KClass<TService>, private val eventBus: EventBus )
{
    /**
     * Publish the specified [event].
     */
    suspend fun publish( event: TEvent ) = eventBus.publish( serviceKlass, event )

    /**
     * Register a [handler] for events of [eventType] belonging to [applicationServiceKlass].
     *
     * @throws IllegalStateException when [activateHandlers] has already been called.
     */
    fun <
        TOtherService : ApplicationService<TOtherService, TOtherServiceEvent>,
        TOtherServiceEvent : IntegrationEvent<TOtherService>> registerHandler(
        applicationServiceKlass: KClass<TOtherService>,
        eventType: KClass<TOtherServiceEvent>,
        handler: suspend (TOtherServiceEvent) -> Unit
    ) = eventBus.registerHandler( applicationServiceKlass, eventType, serviceKlass, handler )

    /**
     * Start the event subscription for all registered handlers of the application service linked to this event bus.
     *
     * TODO: This should be abstracted away.
     */
    fun activateHandlers() = eventBus.activateHandlers( serviceKlass )
}

/**
 * Register a [handler] for events of type [TEvent] on this [ApplicationServiceEventBus].
 * 
 * @throws IllegalStateException when `activateHandlers` has already been called.
 */
inline fun <
    reified TService : ApplicationService<TService, TEvent>,
    reified TEvent : IntegrationEvent<TService>
> ApplicationServiceEventBus<*, *>.registerHandler( noinline handler: suspend (TEvent) -> Unit ) =
    this.registerHandler( TService::class, TEvent::class, handler )


/**
 * Create an adapter for this [EventBus] which can only publish events associated with [serviceKlass].
 */
fun <
    TService : ApplicationService<TService, TEvent>,
    TEvent : IntegrationEvent<TService>
> EventBus.createApplicationServiceAdapter(
    serviceKlass: KClass<TService>
): ApplicationServiceEventBus<TService, TEvent> = ApplicationServiceEventBus( serviceKlass, this )
