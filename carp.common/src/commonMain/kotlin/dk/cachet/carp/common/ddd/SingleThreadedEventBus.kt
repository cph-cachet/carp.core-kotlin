package dk.cachet.carp.common.ddd

import kotlin.reflect.KClass


/**
 * A simple [EventBus] implementation for testing purposes which publishes and subscribes to events on the same thread.
 */
class SingleThreadedEventBus : EventBus
{
    private class Handler( val eventType: KClass<*>, val handler: suspend (IntegrationEvent<*>) -> Unit )


    private val eventHandlers: MutableList<Handler> = mutableListOf()

    /**
     * Publish the specified [event] belonging to [applicationServiceKlass]
     * and instantly deliver it to all subscribers on the same thread as it is published on.
     */
    override suspend fun <
        TApplicationService : ApplicationService<TApplicationService, TEvent>,
        TEvent : IntegrationEvent<TApplicationService>
    > publish( applicationServiceKlass: KClass<TApplicationService>, event: TEvent )
    {
        val matchingTypes = eventHandlers.filter { it.eventType.isInstance( event ) }
        matchingTypes.forEach { it.handler( event ) }
    }

    /**
     * Subscribe to events of [eventType] belonging to [applicationServiceKlass] and handle them using [handler].
     */
    override fun <
        TApplicationService : ApplicationService<TApplicationService, TEvent>,
        TEvent : IntegrationEvent<TApplicationService>
    > subscribe( applicationServiceKlass: KClass<TApplicationService>, eventType: KClass<TEvent>, handler: suspend (TEvent) -> Unit )
    {
        @Suppress("UNCHECKED_CAST")
        val baseHandler = handler as suspend (IntegrationEvent<*>) -> Unit

        eventHandlers.add( Handler( eventType, baseHandler ) )
    }
}
