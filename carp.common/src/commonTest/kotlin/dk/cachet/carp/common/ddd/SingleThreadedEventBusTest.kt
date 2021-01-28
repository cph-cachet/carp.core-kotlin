package dk.cachet.carp.common.ddd

import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [SingleThreadedEventBus].
 */
class SingleThreadedEventBusTest
{
    interface TestService : ApplicationService<TestService, BaseIntegrationEvent>

    @Serializable
    sealed class BaseIntegrationEvent : IntegrationEvent<TestService>()
    {
        @Serializable
        data class SomeIntegrationEvent( val data: String ) : BaseIntegrationEvent()

        @Serializable
        data class AnotherIntegrationEvent( val data: String ) : BaseIntegrationEvent()
    }


    @Test
    fun published_events_are_received_by_subscribers() = runSuspendTest {
        val bus = SingleThreadedEventBus()

        var receivedEventData: String? = null
        bus.subscribe( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent::class ) { event ->
            receivedEventData = event.data
        }
        val sentData = "Data"

        bus.publish( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent( sentData ) )
        assertEquals( "Data", receivedEventData )
    }

    @Test
    fun subscribers_only_receive_requested_events() = runSuspendTest {
        val bus = SingleThreadedEventBus()

        var eventReceived = false
        bus.subscribe( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent::class ) { eventReceived = true }

        bus.publish( TestService::class, BaseIntegrationEvent.AnotherIntegrationEvent( "Test" ) )
        assertFalse( eventReceived )
    }

    @Test
    fun multiple_subscribers_are_possible() = runSuspendTest {
        val bus = SingleThreadedEventBus()

        var receivedBySubscriber1 = false
        bus.subscribe( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent::class ) { receivedBySubscriber1 = true }
        var receivedBySubscriber2 = false
        bus.subscribe( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent::class ) { receivedBySubscriber2 = true }

        bus.publish( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent( "Test" ) )
        assertTrue( receivedBySubscriber1 )
        assertTrue( receivedBySubscriber2 )
    }

    @Test
    fun polymorphic_subscribers_are_possible() = runSuspendTest {
        val bus = SingleThreadedEventBus()

        var receivedEvent = false
        bus.subscribe( TestService::class, BaseIntegrationEvent::class )
        {
            if ( it is BaseIntegrationEvent.SomeIntegrationEvent ) receivedEvent = true
        }

        bus.publish( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent( "Test" ) )
        assertTrue( receivedEvent )
    }

    @Test
    fun extension_methods_succeeds() = runSuspendTest {
        val bus = SingleThreadedEventBus()

        var receivedData: String? = null
        bus.subscribe { event: BaseIntegrationEvent.SomeIntegrationEvent ->
            receivedData = event.data
        }
        bus.publish( BaseIntegrationEvent.SomeIntegrationEvent( "Test" ) )

        assertEquals( "Test", receivedData )
    }
}
