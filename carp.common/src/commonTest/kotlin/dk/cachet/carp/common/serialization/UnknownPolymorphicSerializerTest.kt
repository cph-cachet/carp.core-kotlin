package dk.cachet.carp.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.subclass
import kotlin.test.*


/**
 * Tests for [UnknownPolymorphicSerializer].
 */
class UnknownPolymorphicSerializerTest
{
    val SERIAL_MODULE = SerializersModule {
        polymorphic( BaseType::class )
        {
            subclass( DerivingType::class )
        }
    }

    @Serializable
    @Polymorphic
    abstract class BaseType
    {
        abstract val toOverrideProperty: String

        val baseProperty: Boolean = true
    }

    @Serializable
    class DerivingType( override val toOverrideProperty: String ) : BaseType()

    data class CustomBaseType( override val className: String, override val jsonSource: String, val serializer: Json ) :
        BaseType(), UnknownPolymorphicWrapper
    {
        override val toOverrideProperty: String

        init
        {
            val json = serializer.parseToJsonElement( jsonSource ) as JsonObject

            val toOverridePropertyField = BaseType::toOverrideProperty.name
            require( json.containsKey( toOverridePropertyField ) ) { "No '$toOverridePropertyField' defined." }
            toOverrideProperty = json[ toOverridePropertyField ]!!.jsonPrimitive.content
        }
    }

    object UnknownBaseTypeSerializer : KSerializer<BaseType>
        by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomBaseType( className, json, serializer ) } )


    private fun initializeJson() = Json {
        // TODO: Rather than hardcoding the class discriminator, get it from the `json.configuration`.
        classDiscriminator = CLASS_DISCRIMINATOR
        serializersModule = SERIAL_MODULE
    }

    @Test
    fun base_properties_are_serialized()
    {
        val json = initializeJson()
        val knownType: BaseType = DerivingType( "Test" )

        val serialized = json.encodeToString( UnknownBaseTypeSerializer, knownType )

        assertTrue( serialized.contains( BaseType::baseProperty.name ) )
    }

    @Test
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val json = initializeJson()
        val toSerialize = DerivingType( "Test" )
        val serialized = json.encodeToString( UnknownBaseTypeSerializer, toSerialize )
        val unknownSerialized = serialized.replace( DerivingType::class.simpleName!!, "UnknownType" )

        val parsed = json.decodeFromString( UnknownBaseTypeSerializer, unknownSerialized )
        assertTrue( parsed is CustomBaseType )
        assertEquals( "dk.cachet.carp.common.serialization.UnknownPolymorphicSerializerTest.UnknownType", parsed.className )
        val expectedJsonSource = json.encodeToString( BaseType.serializer(), toSerialize )
            .replace( DerivingType::class.simpleName!!, "UnknownType" )
        assertEquals( expectedJsonSource, parsed.jsonSource )
    }
}
