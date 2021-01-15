package dk.cachet.carp.common.ddd

import kotlin.reflect.KClass


/**
 * A simple [EventBus] implementation for testing purposes which publishes and subscribes to events on the same thread.
 */
class SingleThreadedEventBus : EventBus
{
    private class Handler( val eventType: KClass<*>, val handler: (IntegrationEvent) -> Unit )


    private val eventHandlers: MutableList<Handler> = mutableListOf()

    /**
     * Publish the specified [event] and instantly deliver it to all subscribers on the same thread as it is published on.
     */
    override suspend fun publish( event: IntegrationEvent )
    {
        val matchingTypes = eventHandlers.filter { it.eventType.isInstance( event ) }
        matchingTypes.forEach { it.handler( event ) }
    }

    /**
     * Subscribe to events of [eventType] and handle them using [handler].
     */
    override suspend fun <TEvent : IntegrationEvent> subscribe( eventType: KClass<TEvent>, handler: (TEvent) -> Unit )
    {
        @Suppress("UNCHECKED_CAST")
        val baseHandler = handler as (IntegrationEvent) -> Unit

        eventHandlers.add( Handler( eventType, baseHandler ) )
    }
}
