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
        TApplicationService : ApplicationService<TApplicationService, TEvent>,
        TEvent : IntegrationEvent<TApplicationService>
    > publish( applicationServiceKlass: KClass<TApplicationService>, event: TEvent )

    /**
     * Subscribe to events of [eventType] belonging to [applicationServiceKlass] and handle them using [handler].
     */
    suspend fun <
        TApplicationService : ApplicationService<TApplicationService, TEvent>,
        TEvent : IntegrationEvent<TApplicationService>
    > subscribe( applicationServiceKlass: KClass<TApplicationService>, eventType: KClass<TEvent>, handler: (TEvent) -> Unit )
}


/**
 * Publish the specified [event] on this [EventBus].
 */
suspend inline fun <
    reified TApplicationService : ApplicationService<TApplicationService, TEvent>,
    reified TEvent : IntegrationEvent<TApplicationService>
> EventBus.publish( event: TEvent ) =
    this.publish( TApplicationService::class, event )

/**
 * Subscribe to events of type [TEvent] on this [EventBus] and handle them using [handler].
 */
suspend inline fun <
    reified TApplicationService : ApplicationService<TApplicationService, TEvent>,
    reified TEvent : IntegrationEvent<TApplicationService>
> EventBus.subscribe( noinline handler: (TEvent) -> Unit ) =
    this.subscribe( TApplicationService::class, TEvent::class, handler )
