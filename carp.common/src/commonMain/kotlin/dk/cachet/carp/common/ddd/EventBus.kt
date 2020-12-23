package dk.cachet.carp.common.ddd

import kotlin.reflect.KClass


/**
 * A message bus with a publish/subscribe mechanism to distribute integration events across application services.
 */
interface EventBus
{
    /**
     * Publish the specified [event].
     */
    suspend fun publish( event: IntegrationEvent )

    /**
     * Subscribe to events of [eventType] and handle them using [handler].
     */
    suspend fun <TEvent : IntegrationEvent> subscribe( eventType: KClass<TEvent>, handler: (TEvent) -> Unit )
}


/**
 * Subscribe to events of type [TEvent] on the specified [eventBus] and handle them using [handler].
 */
suspend inline fun <reified TEvent : IntegrationEvent> subscribeEvent(
    eventBus: EventBus,
    noinline handler: (TEvent) -> Unit
)
{
    val eventType = TEvent::class
    eventBus.subscribe( eventType, handler )
}
