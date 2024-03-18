package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.*
import kotlin.test.*


/**
 * Tests for helper functions which create primitive serializers for CARP common types.
 */
class CarpPrimitiveSerializerTest
{
    @Serializable( PrimitiveStringSerializer::class )
    class PrimitiveString( val string: String )
    {
        override fun toString(): String = string
    }
    object PrimitiveStringSerializer : KSerializer<PrimitiveString> by
        createCarpStringPrimitiveSerializer( { PrimitiveString( it ) } )

    @Test
    fun createCarpStringPrimitiveSerializer_serializes_as_string()
    {
        val primitive = PrimitiveString( "Test" )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( primitive )

        assertEquals( "\"Test\"", serialized ) // Serialized with quotes.
    }

    @ExperimentalSerializationApi
    @Test
    fun createCarpStringPrimitiveSerializer_uses_carp_namespace_serialName()
    {
        val serializer = createCarpStringPrimitiveSerializer { PrimitiveString( it ) }

        assertEquals( "dk.cachet.carp.common.PrimitiveString", serializer.descriptor.serialName )
    }


    @Serializable( PrimitiveLongSerializer::class )
    class PrimitiveLong( val long: Long )
    object PrimitiveLongSerializer : KSerializer<PrimitiveLong> by
        createCarpLongPrimitiveSerializer( { PrimitiveLong( it ) }, { it.long } )

    @Test
    fun createCarpLongPrimitiveSerializer_serializes_as_long()
    {
        val primitive = PrimitiveLong( 42 )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( primitive )

        assertEquals( "42", serialized ) // Serialized without quotes.
    }

    @ExperimentalSerializationApi
    @Test
    fun createCarpLongPrimitiveSerializer_uses_carp_namespace_serialName()
    {
        val serializer = createCarpLongPrimitiveSerializer( { PrimitiveLong( it ) }, { it.long } )

        assertEquals( "dk.cachet.carp.common.PrimitiveLong", serializer.descriptor.serialName )
    }
}
