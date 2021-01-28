package dk.cachet.carp.common.ddd

import kotlin.reflect.KClass


/**
 * A message bus with a publish/subscribe mechanism to distribute integration events across application services.
 */
interface EventBus
{
    /**
     * Publish the specified [event] belonging to [applicationServiceKlass].
     */
    suspend fun <
        TService : ApplicationService<TService, TEvent>,
        TEvent : IntegrationEvent<TService>
    > publish( applicationServiceKlass: KClass<TService>, event: TEvent )

    /**
     * Subscribe to events of [eventType] belonging to [applicationServiceKlass] and handle them using [handler].
     */
    fun <
        TService : ApplicationService<TService, TEvent>,
        TEvent : IntegrationEvent<TService>
    > subscribe( applicationServiceKlass: KClass<TService>, eventType: KClass<TEvent>, handler: suspend (TEvent) -> Unit )
}


/**
 * Publish the specified [event] on this [EventBus].
 */
suspend inline fun <
    reified TService : ApplicationService<TService, TEvent>,
    reified TEvent : IntegrationEvent<TService>
> EventBus.publish( event: TEvent ) =
    this.publish( TService::class, event )

/**
 * Subscribe to events of type [TEvent] on this [EventBus] and handle them using [handler].
 */
inline fun <
    reified TService : ApplicationService<TService, TEvent>,
    reified TEvent : IntegrationEvent<TService>
> EventBus.subscribe( noinline handler: suspend (TEvent) -> Unit ) =
    this.subscribe( TService::class, TEvent::class, handler )
