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
     * Register a [handler] for events of [eventType] belonging to [publishingServiceKlass],
     * to be received by [consumingService].
     *
     * @throws IllegalStateException when trying to register a handler for a [consumingService]
     *   for which [activateHandlers] has already been called.
     */
    fun <
        TService : ApplicationService<TService, TEvent>,
        TEvent : IntegrationEvent<TService>> registerHandler(
        publishingServiceKlass: KClass<TService>,
        eventType: KClass<TEvent>,
        consumingService: Any,
        handler: suspend (TEvent) -> Unit
    )

    /**
     * Start the event subscription for all registered handlers of [consumingService].
     *
     * @throws IllegalStateException when this is called more than once.
     */
    fun activateHandlers( consumingService: Any )
}


/**
 * Publish the specified [event] on this [EventBus].
 */
suspend inline fun <
    reified TService : ApplicationService<TService, TEvent>,
    reified TEvent : IntegrationEvent<TService>
> EventBus.publish( event: TEvent ) = this.publish( TService::class, event )

/**
 * Register a [handler] to be received by [consumingService] for events of type [TEvent] on this [EventBus].
 *
 * @throws IllegalStateException when trying to register a handler for a [consumingService]
 *   for which `activateHandlers` has already been called.
 */
inline fun <
    reified TService : ApplicationService<TService, TEvent>,
    reified TEvent : IntegrationEvent<TService>
> EventBus.registerHandler( consumingService: Any, noinline handler: suspend (TEvent) -> Unit ) =
    this.registerHandler( TService::class, TEvent::class, consumingService, handler )
