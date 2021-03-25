package dk.cachet.carp.common.ddd

import kotlin.reflect.KClass


/**
 * A simple [EventBus] implementation for testing purposes which publishes and subscribes to events on the same thread.
 */
class SingleThreadedEventBus : EventBus()
{
    /**
     * Start the event subscription for [subscriber] using the specified [handlers].
     */
    override fun activateHandlers( subscriber: Any, handlers: List<Handler> )
    {
        // Nothing to do.
    }

    /**
     * Publish the specified [event] belonging to [publishingService]
     * and instantly deliver it to all subscribers on the same thread as it is published on.
     */
    override suspend fun <
        TApplicationService : ApplicationService<TApplicationService, TEvent>,
        TEvent : IntegrationEvent<TApplicationService>> publish(
        publishingService: KClass<TApplicationService>,
        event: TEvent
    )
    {
        // Find all active handlers listening to the published event type.
        val handlers = subscribers.values
            .filter { it.isActivated }
            .flatMap { it.eventHandlers.filter { handler -> handler.eventType.isInstance( event ) } }

        // Publish.
        handlers.forEach { it.handler( event ) }
    }
}
