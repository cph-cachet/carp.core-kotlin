package dk.cachet.carp.common.application.services

import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [ApplicationServiceEventBus].
 */
class ApplicationServiceEventBusTest
{
    interface TestService : ApplicationService<TestService, Event>
    {
        companion object { val API_VERSION = ApiVersion( 1, 0 ) }
    }

    @Serializable
    sealed class Event( override val aggregateId: String? = null ) : IntegrationEvent<TestService>
    {
        @Required
        override val apiVersion: ApiVersion = TestService.API_VERSION

        @Serializable
        object SomeEvent : Event()
    }

    interface OtherService : ApplicationService<OtherService, OtherEvent>
    {
        companion object { val API_VERSION = ApiVersion( 1, 0 ) }
    }

    @Serializable
    sealed class OtherEvent( override val aggregateId: String? = null ) : IntegrationEvent<OtherService>
    {
        @Required
        override val apiVersion: ApiVersion = OtherService.API_VERSION

        @Serializable
        object SomeOtherEvent : OtherEvent()
    }


    @Test
    fun createApplicationServiceAdapter_succeeds()
    {
        val bus = SingleThreadedEventBus()
        bus.createApplicationServiceAdapter( TestService::class )
    }

    @Test
    fun publish_succeeds() = runSuspendTest {
        val bus = SingleThreadedEventBus()
        val serviceBus = bus.createApplicationServiceAdapter( TestService::class )

        var eventReceived = false
        bus.registerHandler( this ) { _: Event.SomeEvent -> eventReceived = true }
        bus.activateHandlers( this )
        serviceBus.publish( Event.SomeEvent )

        assertTrue( eventReceived )
    }

    @Test
    fun subscribe_succeeds() = runSuspendTest {
        val bus = SingleThreadedEventBus()
        val serviceBus = bus.createApplicationServiceAdapter( TestService::class )

        var eventReceived = false
        serviceBus.subscribe {
            event { _: OtherEvent -> eventReceived = true }
        }
        bus.publish( OtherEvent.SomeOtherEvent )

        assertTrue( eventReceived )
    }
}
