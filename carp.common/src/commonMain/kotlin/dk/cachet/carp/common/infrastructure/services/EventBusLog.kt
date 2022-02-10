package dk.cachet.carp.common.infrastructure.services


import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlin.reflect.KClass


/**
 * Logs incoming events on an event bus that are requested to be observed.
 */
class EventBusLog( eventBus: EventBus, vararg toObserve: Subscription<*, *> )
{
    data class Subscription<
        TService : ApplicationService<TService, TEvent>,
        TEvent : IntegrationEvent<TService>
    >( val eventSource: KClass<TService>, val eventType: KClass<TEvent> )
    {
        fun registerHandler( eventBus: EventBus, subscriber: Any, handler: suspend (TEvent) -> Unit ) =
            eventBus.registerHandler( eventSource, eventType, subscriber, handler )
    }


    private val loggedEvents: MutableList<IntegrationEvent<*>> = mutableListOf()

    init
    {
        toObserve.forEach {
            it.registerHandler( eventBus, this ) { event -> loggedEvents.add( event ) }
        }
        eventBus.activateHandlers( this )
    }

    /**
     * Retrieve all elements in the log and clear it after.
     */
    fun retrieveAndEmptyLog(): List<IntegrationEvent<*>> = loggedEvents.toList().also { loggedEvents.clear() }
}
