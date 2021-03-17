package dk.cachet.carp.common.ddd

import kotlin.reflect.KClass


/**
 * A simple [EventBus] implementation for testing purposes which publishes and subscribes to events on the same thread.
 */
class SingleThreadedEventBus : EventBus
{
    private class Handler( val eventType: KClass<*>, val handler: suspend (IntegrationEvent<*>) -> Unit )

    private class EventConsumerState
    {
        val eventHandlers: MutableList<Handler> = mutableListOf()
        var consumerActivated: Boolean = false
    }


    private val eventConsumers: MutableMap<Any, EventConsumerState> = mutableMapOf()

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
        val handlers = eventConsumers.values
            .filter { it.consumerActivated }
            .flatMap { it.eventHandlers.filter { handler -> handler.eventType.isInstance( event ) } }

        // Publish.
        handlers.forEach { it.handler( event ) }
    }

    /**
     * Register a [handler] for events of [eventType] belonging to [publishingServiceKlass],
     * to be received by [consumingService].
     *
     * @throws IllegalStateException when trying to register a handler for a [consumingService]
     *   for which [activateHandlers] has already been called.
     */
    override fun <
        TApplicationService : ApplicationService<TApplicationService, TEvent>,
        TEvent : IntegrationEvent<TApplicationService>> registerHandler(
        publishingServiceKlass: KClass<TApplicationService>,
        eventType: KClass<TEvent>,
        consumingService: Any,
        handler: suspend (TEvent) -> Unit
    )
    {
        val consumerState = eventConsumers.getOrPut( consumingService ) { EventConsumerState() }
        check( !consumerState.consumerActivated )
            { "Cannot register event handlers after handlers for consuming service have been activated." }

        @Suppress("UNCHECKED_CAST")
        val baseHandler = handler as suspend (IntegrationEvent<*>) -> Unit

        consumerState.eventHandlers.add( Handler( eventType, baseHandler ) )
    }

    /**
     * Start the event subscription for all registered handlers of [consumingService].
     *
     * @throws IllegalStateException when this is called more than once.
     */
    override fun activateHandlers( consumingService: Any )
    {
        val consumerState = eventConsumers.getOrPut( consumingService ) { EventConsumerState() }
        check( !consumerState.consumerActivated ) { "Can only activate handlers for consuming service once." }

        consumerState.consumerActivated = true
    }
}
