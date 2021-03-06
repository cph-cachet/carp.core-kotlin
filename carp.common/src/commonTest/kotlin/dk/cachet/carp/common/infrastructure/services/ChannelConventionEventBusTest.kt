package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.application.services.publish
import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.test.*


/**
 * Tests for [ChannelConventionEventBus].
 */
class ChannelConventionEventBusTest
{
    class StubChannelConventionEventBus : ChannelConventionEventBus()
    {
        val channelHandlers: MutableMap<KClass<*>, suspend (IntegrationEvent<*>) -> Unit> = mutableMapOf()

        override suspend fun <TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>> publishToChannel(
            channelIdentifier: KClass<TService>,
            event: TEvent
        ) = channelHandlers[ channelIdentifier ]!!.invoke( event )

        override fun subscribeToChannel( channelIdentifier: KClass<*>, handler: suspend (IntegrationEvent<*>) -> Unit )
        {
            channelHandlers[ channelIdentifier ] = handler
        }
    }

    interface TestService1 : ApplicationService<TestService1, Service1Event>

    @Serializable
    sealed class Service1Event : IntegrationEvent<TestService1>()
    {
        @Serializable
        data class SomeEvent( val data: String ) : Service1Event()

        @Serializable
        data class SomeOtherEvent( val data: String ) : Service1Event()
    }

    interface TestService2 : ApplicationService<TestService2, Service2Event>

    @Serializable
    sealed class Service2Event : IntegrationEvent<TestService2>()
    {
        @Serializable
        data class SomeEvent( val data: String ) : Service2Event()
    }


    @Test
    fun activeHandlers_redirects_to_subscribeToChannel()
    {
        val eventBus = StubChannelConventionEventBus()

        // No channel subscriptions registered until activateHandlers is called.
        eventBus.registerHandler( TestService1::class, Service1Event.SomeEvent::class, this ) { }
        eventBus.registerHandler( TestService1::class, Service1Event.SomeOtherEvent::class, this ) { }
        eventBus.registerHandler( TestService2::class, Service2Event.SomeEvent::class, this ) { }
        assertTrue( eventBus.channelHandlers.isEmpty() )

        // subscribeToChannel is grouped per application service.
        eventBus.activateHandlers( this )
        assertEquals( 2, eventBus.channelHandlers.size )
        assertTrue( eventBus.channelHandlers.containsKey( TestService1::class ) )
        assertTrue( eventBus.channelHandlers.containsKey( TestService2::class ) )
    }

    @Test
    fun published_events_are_received_by_handlers() = runSuspendTest {
        val eventBus = StubChannelConventionEventBus()

        var eventReceived: String? = null
        eventBus.registerHandler( TestService1::class, Service1Event.SomeEvent::class, this )
        {
            eventReceived = it.data
        }
        eventBus.activateHandlers( this )

        val sentData = "Test"
        eventBus.publish( Service1Event.SomeEvent( sentData ) )
        assertEquals( sentData, eventReceived )
    }
}
