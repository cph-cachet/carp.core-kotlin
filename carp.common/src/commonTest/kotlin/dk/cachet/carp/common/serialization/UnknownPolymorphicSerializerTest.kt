package dk.cachet.carp.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.*


/**
 * Tests for [UnknownPolymorphicSerializer].
 */
class UnknownPolymorphicSerializerTest
{
    val SERIAL_MODULE = SerializersModule {
        polymorphic( BaseType::class )
        {
            DerivingType::class with DerivingType.serializer()
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
            val json = serializer.parseJson( jsonSource ) as JsonObject

            val toOverridePropertyField = BaseType::toOverrideProperty.name
            require( json.containsKey( toOverridePropertyField ) ) { "No '$toOverridePropertyField' defined." }
            toOverrideProperty = json[ toOverridePropertyField ]!!.content
        }
    }

    object UnknownBaseTypeSerializer : KSerializer<BaseType>
        by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomBaseType( className, json, serializer ) } )


    private fun initializeJson() = Json( JsonConfiguration.Stable, SERIAL_MODULE )

    @Test
    fun base_properties_are_serialized()
    {
        val json = initializeJson()
        val knownType: BaseType = DerivingType( "Test" )

        val serialized = json.stringify( UnknownBaseTypeSerializer, knownType )

        assertTrue( serialized.contains( BaseType::baseProperty.name ) )
    }

    @Test
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val json = initializeJson()
        val toSerialize = DerivingType( "Test" )
        val serialized = json.stringify( UnknownBaseTypeSerializer, toSerialize )
        val unknownSerialized = serialized.replace( DerivingType::class.simpleName!!, "UnknownType" )

        val parsed = json.parse( UnknownBaseTypeSerializer, unknownSerialized )
        assertTrue( parsed is CustomBaseType )
        assertEquals( "dk.cachet.carp.common.serialization.UnknownPolymorphicSerializerTest.UnknownType", parsed.className )
        val expectedJsonSource = json.stringify( DerivingType.serializer(), toSerialize )
        assertEquals( expectedJsonSource, parsed.jsonSource )
    }
}
