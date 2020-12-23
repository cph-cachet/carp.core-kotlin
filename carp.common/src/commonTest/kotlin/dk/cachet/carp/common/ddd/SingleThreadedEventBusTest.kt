package dk.cachet.carp.common.ddd

import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [SingleThreadedEventBus].
 */
class SingleThreadedEventBusTest
{
    @Serializable
    data class SomeIntegrationEvent( val data: String ) : IntegrationEvent()

    @Serializable
    data class AnotherIntegrationEvent( val data: String ) : IntegrationEvent()

    @Test
    fun published_events_are_received_by_subscribers() = runSuspendTest {
        val bus = SingleThreadedEventBus()

        var receivedEventData: String? = null
        bus.subscribe( SomeIntegrationEvent::class ) { event -> receivedEventData = event.data }
        val sentData = "Data"

        bus.publish( SomeIntegrationEvent( sentData ) )
        assertEquals( "Data", receivedEventData )
    }

    @Test
    fun subscribers_only_receive_requested_events() = runSuspendTest {
        val bus = SingleThreadedEventBus()

        var eventReceived = false
        bus.subscribe( SomeIntegrationEvent::class ) { eventReceived = true }

        bus.publish( AnotherIntegrationEvent( "Test" ) )
        assertFalse( eventReceived )
    }

    @Test
    fun multiple_subscribers_are_possible() = runSuspendTest {
        val bus = SingleThreadedEventBus()

        var receivedBySubscriber1 = false
        bus.subscribe( SomeIntegrationEvent::class ) { receivedBySubscriber1 = true }
        var receivedBySubscriber2 = false
        bus.subscribe( SomeIntegrationEvent::class ) { receivedBySubscriber2 = true }

        bus.publish( SomeIntegrationEvent( "Test" ) )
        assertTrue( receivedBySubscriber1 )
        assertTrue( receivedBySubscriber2 )
    }

    @Test
    fun subscribeEvent_succeeds() = runSuspendTest {
        val bus = SingleThreadedEventBus()

        var receivedData: String? = null
        subscribeEvent( bus ) { event: SomeIntegrationEvent ->
            receivedData = event.data
        }
        bus.publish( SomeIntegrationEvent( "Test" ) )

        assertEquals( "Test", receivedData )
    }
}
