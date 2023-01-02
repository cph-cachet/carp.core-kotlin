package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.application.services.publish
import dk.cachet.carp.common.application.services.registerHandler
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.*
import kotlin.test.*


/**
 * Tests for [SingleThreadedEventBus].
 */
class SingleThreadedEventBusTest
{
    interface TestService : ApplicationService<TestService, BaseIntegrationEvent>
    {
        companion object { val API_VERSION = ApiVersion( 1, 0 ) }
    }

    @Serializable
    sealed class BaseIntegrationEvent( override val aggregateId: String? = null ) : IntegrationEvent<TestService>
    {
        @Required
        override val apiVersion: ApiVersion = TestService.API_VERSION

        @Serializable
        data class SomeIntegrationEvent( val data: String ) : BaseIntegrationEvent()

        @Serializable
        data class AnotherIntegrationEvent( val data: String ) : BaseIntegrationEvent()
    }


    @Test
    fun published_events_are_received_by_handlers() = runTest {
        val bus = SingleThreadedEventBus()

        var receivedEventData: String? = null
        bus.registerHandler( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent::class, this ) { event ->
            receivedEventData = event.data
        }
        bus.activateHandlers( this )
        val sentData = "Data"

        bus.publish( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent( sentData ) )
        assertEquals( sentData, receivedEventData )
    }

    @Test
    fun handlers_only_receive_events_when_activated() = runTest {
        val bus = SingleThreadedEventBus()

        var receivedEventData: String? = null
        bus.registerHandler( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent::class, this ) { event ->
            receivedEventData = event.data
        }

        bus.publish( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent( "Data" ) )
        assertNull( receivedEventData )
    }

    @Test
    fun handlers_only_receive_requested_events() = runTest {
        val bus = SingleThreadedEventBus()

        var eventReceived = false
        bus.registerHandler( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent::class, this ) {
            eventReceived = true
        }
        bus.activateHandlers( this )

        bus.publish( TestService::class, BaseIntegrationEvent.AnotherIntegrationEvent( "Test" ) )
        assertFalse( eventReceived )
    }


    @Test
    fun multiple_handlers_are_possible() = runTest {
        val bus = SingleThreadedEventBus()

        var receivedBySubscriber1 = false
        bus.registerHandler( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent::class, this ) { receivedBySubscriber1 = true }
        var receivedBySubscriber2 = false
        bus.registerHandler( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent::class, this ) { receivedBySubscriber2 = true }
        bus.activateHandlers( this )

        bus.publish( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent( "Test" ) )
        assertTrue( receivedBySubscriber1 )
        assertTrue( receivedBySubscriber2 )
    }

    @Test
    fun polymorphic_handlers_are_possible() = runTest {
        val bus = SingleThreadedEventBus()

        var receivedEvent = false
        bus.registerHandler( TestService::class, BaseIntegrationEvent::class, this )
        {
            if ( it is BaseIntegrationEvent.SomeIntegrationEvent ) receivedEvent = true
        }
        bus.activateHandlers( this )

        bus.publish( TestService::class, BaseIntegrationEvent.SomeIntegrationEvent( "Test" ) )
        assertTrue( receivedEvent )
    }

    @Test
    fun registerHandler_fails_for_consumingService_with_activated_handlers()
    {
        val bus = SingleThreadedEventBus()
        bus.activateHandlers( this )

        assertFailsWith<IllegalStateException> {
            bus.registerHandler( TestService::class, BaseIntegrationEvent::class, this ) { }
        }
    }

    @Test
    fun activateHandlers_fails_when_already_activated()
    {
        val bus = SingleThreadedEventBus()
        bus.activateHandlers( this )

        assertFailsWith<IllegalStateException> { bus.activateHandlers( this ) }
    }

    @Test
    fun extension_methods_succeeds() = runTest {
        val bus = SingleThreadedEventBus()

        var receivedData: String? = null
        bus.registerHandler( this ) { event: BaseIntegrationEvent.SomeIntegrationEvent ->
            receivedData = event.data
        }
        bus.activateHandlers( this )
        bus.publish( BaseIntegrationEvent.SomeIntegrationEvent( "Test" ) )

        assertEquals( "Test", receivedData )
    }

    @Test
    fun registerHandler_extension_method_fails_for_consumingService_with_activated_handlers()
    {
        val bus = SingleThreadedEventBus()
        bus.activateHandlers( this )

        assertFailsWith<IllegalStateException> {
            bus.registerHandler( this ) { _: BaseIntegrationEvent -> }
        }
    }
}
