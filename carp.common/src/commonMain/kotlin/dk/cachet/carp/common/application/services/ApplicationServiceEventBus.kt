package dk.cachet.carp.common.application.services

import kotlin.reflect.KClass


/**
 * An event bus which can be used by an [ApplicationService] to publish events associated with its service
 * and subscribe to any other events.
 */
class ApplicationServiceEventBus<
    TService : ApplicationService<TService, TEvent>,
    TEvent : IntegrationEvent<TService>
>( private val serviceKlass: KClass<TService>, private val eventBus: EventBus )
{
    /**
     * Publish the specified [event].
     */
    suspend fun publish( event: TEvent ) = eventBus.publish( serviceKlass, event )

    /**
     * Subscribe to a set of events, registered by calling `event`.
     * This can only be called once.
     *
     * @throws IllegalStateException when subscribe has already been called.
     */
    fun subscribe( registerHandlers: EventSubscriptionBuilder.() -> Unit )
    {
        val builder = EventSubscriptionBuilder( serviceKlass, eventBus )
        builder.registerHandlers()
        eventBus.activateHandlers( serviceKlass )
    }
}


@DslMarker
annotation class EventSubscriptionDsl

// TODO: Apply the DSL marker to `event` to disallow calling `event` from inside `event`.
//  For that to work `event` should be a builder itself to which the marker can be applied.
//  That is currently impossible since we cannot extend from `suspend (TEvent) -> Unit`:
//  https://youtrack.jetbrains.com/issue/KT-18707
@EventSubscriptionDsl
class EventSubscriptionBuilder(
    @PublishedApi
    internal val subscriber: Any,
    @PublishedApi
    internal val eventBus: EventBus
)
{
    /**
     * Register a [handler] for events of type [TEvent] on this [ApplicationServiceEventBus].
     */
    inline fun <
        reified TService : ApplicationService<TService, TEvent>,
        reified TEvent : IntegrationEvent<TService>
    > event(
        noinline handler: suspend (TEvent) -> Unit
    ) = eventBus.registerHandler( TService::class, TEvent::class, subscriber, handler )
}


/**
 * Create an adapter for this [EventBus] which can only publish events associated with [serviceKlass].
 */
fun <
    TService : ApplicationService<TService, TEvent>,
    TEvent : IntegrationEvent<TService>
> EventBus.createApplicationServiceAdapter(
    serviceKlass: KClass<TService>
): ApplicationServiceEventBus<TService, TEvent> = ApplicationServiceEventBus( serviceKlass, this )
