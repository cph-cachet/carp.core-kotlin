package dk.cachet.carp.common.serialization

import kotlinx.serialization.*
import kotlinx.serialization.json.*
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

    data class CustomBaseType( override val className: String, override val jsonSource: String, val serializer: Json )
        : BaseType(), UnknownPolymorphicWrapper
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


    @Test
    fun base_properties_are_serialized()
    {
        val json = Json( JsonConfiguration.Stable, SERIAL_MODULE )
        val knownType: BaseType = DerivingType( "Test" )

        val serialized = json.stringify( UnknownBaseTypeSerializer, knownType )

        assertTrue( serialized.contains( "baseProperty" ) )
    }
}