package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.test.*


/**
 * Tests for [IntegrationEvent].
 */
class IntegrationEventTest
{
    interface Service : ApplicationService<Service, Service.Event>
    {
        @Serializable
        sealed class Event : IntegrationEvent<Service>()
        {
            @Serializable
            object SomeEvent : Event()
        }
    }


    @Test
    fun can_serialize_and_deserialize_application_service_event()
    {
        val event = Service.Event.SomeEvent

        val json = Json { }
        val serializer = Service.Event.serializer()
        val serialized = json.encodeToString( serializer, event )
        val parsed = json.decodeFromString( serializer, serialized )

        assertEquals( event, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_base_IntegrationEvent()
    {
        val event: IntegrationEvent<*> = Service.Event.SomeEvent

        val module = SerializersModule {
            polymorphic( IntegrationEvent::class )
            {
                subclass( Service.Event.SomeEvent::class, Service.Event.SomeEvent.serializer() )
            }
        }
        val json = Json { serializersModule = module }
        val serializer = IntegrationEvent.serializer( NotSerializable )
        val serialized = json.encodeToString( serializer, event )
        val parsed = json.decodeFromString( serializer, serialized )

        assertEquals( event, parsed )
    }
}
