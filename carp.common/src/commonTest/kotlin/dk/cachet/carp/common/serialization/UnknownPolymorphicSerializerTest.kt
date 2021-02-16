package dk.cachet.carp.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.*


/**
 * Tests for [UnknownPolymorphicSerializer].
 */
class UnknownPolymorphicSerializerTest
{
    private val testModule = SerializersModule {
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
    data class DerivingType( override val toOverrideProperty: String ) : BaseType()

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


    private fun initializeJson( classDiscriminator: String = "type" ) = Json {
        this.classDiscriminator = classDiscriminator
        serializersModule = testModule
        // TODO: `encodeDefaults` changed in kotlinx.serialization 1.0.0-RC2 to false by default
        //  which caused unknown polymorphic serializer tests to fail. Verify whether we need this.
        encodeDefaults = true
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

    @Test
    fun custom_class_discriminator_is_supported()
    {
        val classDiscriminator = "---type---"
        val json = initializeJson( classDiscriminator )
        val toSerialize = DerivingType( "Test" )

        val serialized = json.encodeToString( UnknownBaseTypeSerializer, toSerialize )
        assertTrue( serialized.contains( classDiscriminator ) )

        val unknownSerialized = serialized.replace( DerivingType::class.simpleName!!, "UnknownType" )
        val parsed = json.decodeFromString( UnknownBaseTypeSerializer, unknownSerialized )
        assertTrue( parsed is CustomBaseType )
    }

    @Test
    fun fail_when_array_polymorphism_is_configured()
    {
        val toSerialize = DerivingType( "Test" )

        val invalidJson = Json {
            useArrayPolymorphism = true
            serializersModule = testModule
        }
        assertFailsWith<SerializationException> { invalidJson.encodeToString( UnknownBaseTypeSerializer, toSerialize ) }
        assertFailsWith<SerializationException> { invalidJson.decodeFromString( UnknownBaseTypeSerializer, "Irrelevant" ) }
    }

    @Test
    fun supports_non_json_encoders_for_known_types()
    {
        val toSerialize = DerivingType( "Test" )

        val protobuf = ProtoBuf { serializersModule = testModule }
        val encoded = protobuf.encodeToHexString( UnknownBaseTypeSerializer, toSerialize )
        val decoded = protobuf.decodeFromHexString( UnknownBaseTypeSerializer, encoded )
        assertEquals( toSerialize, decoded )
    }

    @Test
    fun does_not_support_non_json_encoders_for_unknown_types()
    {
        class UnregisteredType( override val toOverrideProperty: String ) : BaseType()
        val unregistered = UnregisteredType( "Test" )

        val protobuf = ProtoBuf { serializersModule = testModule }
        assertFailsWith<SerializationException> { protobuf.encodeToHexString( unregistered ) }
    }
}
