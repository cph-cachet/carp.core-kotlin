package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.test.*


/**
 * Tests for [PolymorphicEnumSerializer].
 */
class PolymorphicEnumSerializerTest
{
    interface Base

    @Serializable
    enum class PolyEnum : Base { One }

    /**
     * A [SerializersModule] with the [PolyEnum] registered the default way, with no custom serializer.
     */
    private val defaultRegistrationModule = SerializersModule {
        polymorphic( Base::class )
        {
            subclass( PolyEnum::class )
        }
    }

    @Test
    fun initializing_json_with_class_discriminators_fails_for_polymorphic_enum()
    {
        assertFailsWith<IllegalArgumentException>
        {
            Json {
                useArrayPolymorphism = false
                serializersModule = defaultRegistrationModule
            }
        }
    }

    @Test
    fun initializing_json_with_array_polymorphism_succeeds_for_polymorphic_enum()
    {
        Json {
            useArrayPolymorphism = true
            serializersModule = defaultRegistrationModule
        }
    }

    @Test
    fun can_serialize_and_deserialize_enum_using_PolymorphicEnumSerializer()
    {
        // Json serializer with PolymorphicEnumSerializer used for enum serialization.
        val module = SerializersModule {
            polymorphic( Base::class )
            {
                subclass( PolyEnum::class, PolymorphicEnumSerializer( PolyEnum.serializer() ) )
            }
        }
        val json = Json { serializersModule = module }

        val value: Base = PolyEnum.One

        val polySerializer = PolymorphicSerializer( Base::class )
        val serialized = json.encodeToString( polySerializer, value )
        val parsed = json.decodeFromString( polySerializer, serialized )
        assertEquals( value, parsed )
    }
}
