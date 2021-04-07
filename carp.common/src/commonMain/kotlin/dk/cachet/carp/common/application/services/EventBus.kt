package dk.cachet.carp.common.application.services

import kotlin.reflect.KClass


/**
 * A message bus with a publish/subscribe mechanism to distribute integration events across application services.
 */
abstract class EventBus
{
    /**
     * A [handler] and the associated [eventType] and [eventSource] describing which events it handles.
     */
    protected class Handler(
        val eventSource: KClass<*>,
        val eventType: KClass<*>,
        val handler: suspend (IntegrationEvent<*>) -> Unit
    )

    /**
     * Holds the [eventHandlers] of a subscriber and whether or not the subscriber [isActivated].
     */
    protected class SubscriberState
    {
        val eventHandlers: MutableList<Handler> = mutableListOf()
        var isActivated: Boolean = false
    }


    private val _subscribers: MutableMap<Any, SubscriberState> = mutableMapOf()

    /**
     * All currently registered subscribers, their event handlers, and whether or not they are activated.
     */
    protected val subscribers: Map<Any, SubscriberState>
        get() = _subscribers.toMap()

    /**
     * Register a [handler] for events of [eventType] emitted by [eventSource] to be received by [subscriber].
     *
     * @throws IllegalStateException when trying to register a handler for a [subscriber]
     *   for which [activateHandlers] has already been called.
     */
    fun <
        TService : ApplicationService<TService, TEvent>,
        TEvent : IntegrationEvent<TService>> registerHandler(
        eventSource: KClass<TService>,
        eventType: KClass<TEvent>,
        subscriber: Any,
        handler: suspend (TEvent) -> Unit
    )
    {
        val subscriberState = _subscribers.getOrPut( subscriber ) { SubscriberState() }
        check( !subscriberState.isActivated )
            { "Cannot register event handlers after handlers for subscriber have been activated." }

        @Suppress("UNCHECKED_CAST")
        val baseHandler = handler as suspend (IntegrationEvent<*>) -> Unit

        subscriberState.eventHandlers.add( Handler( eventSource, eventType, baseHandler ) )
    }

    /**
     * Start the event subscription for all registered handlers of [subscriber].
     *
     * @throws IllegalStateException when this is called more than once.
     */
    fun activateHandlers( subscriber: Any )
    {
        val subscriberState = _subscribers.getOrPut( subscriber ) { SubscriberState() }
        check( !subscriberState.isActivated ) { "Can only activate handlers for subscriber once." }

        if ( subscriberState.eventHandlers.isNotEmpty() )
        {
            activateHandlers( subscriber, subscriberState.eventHandlers )
        }
        subscriberState.isActivated = true
    }

    /**
     * Start the event subscription for [subscriber] using the specified [handlers].
     */
    protected abstract fun activateHandlers( subscriber: Any, handlers: List<Handler> )

    /**
     * Publish the specified [event] belonging to [publishingService].
     */
    abstract suspend fun <
        TService : ApplicationService<TService, TEvent>,
        TEvent : IntegrationEvent<TService>
    > publish( publishingService: KClass<TService>, event: TEvent )
}


/**
 * Publish the specified [event] on this [EventBus].
 */
suspend inline fun <
    reified TService : ApplicationService<TService, TEvent>,
    reified TEvent : IntegrationEvent<TService>
> EventBus.publish( event: TEvent ) = this.publish( TService::class, event )

/**
 * Register a [handler] to be received by [subscriber] for events of type [TEvent] on this [EventBus].
 *
 * @throws IllegalStateException when trying to register a handler for a [subscriber]
 *   for which `activateHandlers` has already been called.
 */
inline fun <
    reified TService : ApplicationService<TService, TEvent>,
    reified TEvent : IntegrationEvent<TService>
> EventBus.registerHandler( subscriber: Any, noinline handler: suspend (TEvent) -> Unit ) =
    this.registerHandler( TService::class, TEvent::class, subscriber, handler )
