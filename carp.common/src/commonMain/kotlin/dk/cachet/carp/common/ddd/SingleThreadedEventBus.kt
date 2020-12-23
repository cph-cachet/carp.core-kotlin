package dk.cachet.carp.common.ddd

import kotlin.reflect.KClass


/**
 * A simple [EventBus] implementation for testing purposes which publishes and subscribes to events on the same thread.
 */
class SingleThreadedEventBus : EventBus
{
    private val eventHandlers: MutableMap<KClass<*>, MutableList<(IntegrationEvent) -> Unit>> = mutableMapOf()

    /**
     * Publish the specified [event] and instantly deliver it to all subscribers on the same thread as it is published on.
     */
    override suspend fun publish( event: IntegrationEvent )
    {
        // Get handlers for this event type.
        val eventType = event::class
        val handlers = eventHandlers[ eventType ]

        handlers?.forEach { it( event ) }
    }

    /**
     * Subscribe to events of [eventType] and handle them using [handler].
     */
    override suspend fun <TEvent : IntegrationEvent> subscribe( eventType: KClass<TEvent>, handler: (TEvent) -> Unit )
    {
        val handlers = eventHandlers.getOrPut( eventType, { mutableListOf() } )

        @Suppress("UNCHECKED_CAST")
        handlers.add( handler as (IntegrationEvent) -> Unit )
    }
}
