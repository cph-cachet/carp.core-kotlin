package dk.cachet.carp.common.ddd

import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [ApplicationServiceEventBus].
 */
class ApplicationServiceEventBusTest
{
    interface TestService : ApplicationService<TestService, Event>

    @Serializable
    sealed class Event : IntegrationEvent<TestService>()
    {
        @Serializable
        object SomeEvent : Event()
    }

    interface OtherService : ApplicationService<OtherService, OtherEvent>

    @Serializable
    sealed class OtherEvent : IntegrationEvent<OtherService>()
    {
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
        bus.subscribe { _: Event.SomeEvent -> eventReceived = true }
        serviceBus.publish( Event.SomeEvent )

        assertTrue( eventReceived )
    }

    @Test
    fun subscribe_succeeds() = runSuspendTest {
        val bus = SingleThreadedEventBus()
        val serviceBus = bus.createApplicationServiceAdapter( TestService::class )

        var eventReceived = false
        serviceBus.subscribe { _: OtherEvent -> eventReceived = true }
        bus.publish( OtherEvent.SomeOtherEvent )

        assertTrue( eventReceived )
    }
}
