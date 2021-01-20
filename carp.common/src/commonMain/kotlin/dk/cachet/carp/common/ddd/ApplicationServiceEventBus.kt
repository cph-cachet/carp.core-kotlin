package dk.cachet.carp.common.ddd

import kotlin.reflect.KClass


/**
 * An event bus which can be used by an [ApplicationService] to publish events associated with its service
 * and subscribe to any other events.
 */
class ApplicationServiceEventBus<
    TApplicationService : ApplicationService<TApplicationService, TEvent>,
    TEvent : IntegrationEvent<TApplicationService>
>( private val serviceKlass: KClass<TApplicationService>, private val eventBus: EventBus )
{
    /**
     * Publish the specified [event].
     */
    suspend fun publish( event: TEvent ) = eventBus.publish( serviceKlass, event )

    /**
     * Subscribe to events of [eventType] belonging to [applicationServiceKlass] and handle them using [handler].
     */
    fun <
        TOtherService : ApplicationService<TOtherService, TOtherServiceEvent>,
        TOtherServiceEvent : IntegrationEvent<TOtherService>> subscribe(
        applicationServiceKlass: KClass<TOtherService>,
        eventType: KClass<TOtherServiceEvent>,
        handler: suspend (TOtherServiceEvent) -> Unit
    ) = eventBus.subscribe( applicationServiceKlass, eventType, handler )
}

/**
 * Subscribe to events of type [TEvent] on this [ApplicationServiceEventBus] and handle them using [handler].
 */
inline fun <
    reified TApplicationService : ApplicationService<TApplicationService, TEvent>,
    reified TEvent : IntegrationEvent<TApplicationService>
> ApplicationServiceEventBus<*, *>.subscribe( noinline handler: suspend (TEvent) -> Unit ) =
    this.subscribe( TApplicationService::class, TEvent::class, handler )


/**
 * Create an adapter for this [EventBus] which can only publish events associated with [serviceKlass].
 */
fun <
    TApplicationService : ApplicationService<TApplicationService, TEvent>,
    TEvent : IntegrationEvent<TApplicationService>
> EventBus.createApplicationServiceAdapter(
    serviceKlass: KClass<TApplicationService>
): ApplicationServiceEventBus<TApplicationService, TEvent> = ApplicationServiceEventBus( serviceKlass, this )
