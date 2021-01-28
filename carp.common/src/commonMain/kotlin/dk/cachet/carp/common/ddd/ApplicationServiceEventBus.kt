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
     * Subscribe to events of [eventType] belonging to [applicationServiceKlass] and handle them using [handler].
     */
    fun <
        TOtherService : ApplicationService<TOtherService, TOtherServiceEvent>,
        TOtherServiceEvent : IntegrationEvent<TOtherService>> subscribe(
        applicationServiceKlass: KClass<TOtherService>,
        eventType: KClass<TOtherServiceEvent>,
        handler: suspend (TOtherServiceEvent) -> Unit
    ) = eventBus.subscribe( applicationServiceKlass, eventType, handler )
}

/**
 * Subscribe to events of type [TEvent] on this [ApplicationServiceEventBus] and handle them using [handler].
 */
inline fun <
    reified TService : ApplicationService<TService, TEvent>,
    reified TEvent : IntegrationEvent<TService>
> ApplicationServiceEventBus<*, *>.subscribe( noinline handler: suspend (TEvent) -> Unit ) =
    this.subscribe( TService::class, TEvent::class, handler )


/**
 * Create an adapter for this [EventBus] which can only publish events associated with [serviceKlass].
 */
fun <
    TService : ApplicationService<TService, TEvent>,
    TEvent : IntegrationEvent<TService>
> EventBus.createApplicationServiceAdapter(
    serviceKlass: KClass<TService>
): ApplicationServiceEventBus<TService, TEvent> = ApplicationServiceEventBus( serviceKlass, this )
