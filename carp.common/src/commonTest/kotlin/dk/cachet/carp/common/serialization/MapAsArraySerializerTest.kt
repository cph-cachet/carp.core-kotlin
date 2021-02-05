package dk.cachet.carp.common.serialization

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [MapAsArraySerializer].
 */
class MapAsArraySerializerTest
{
    @Test
    fun serializes_as_array()
    {
        val json = Json.Default

        val map: Map<String, String> = mapOf( "key" to "value" )
        val serializer = MapAsArraySerializer( String.serializer(), String.serializer() )
        val serialized = json.encodeToString( serializer, map )

        assertEquals( """[{"key":"key","value":"value"}]""", serialized )
    }

    @Test
    fun can_serialize_and_deserialize_enum_using_MapAsArraySerializer()
    {
        val json = Json.Default

        val map: Map<String, String> = mapOf( "key.with.dots" to "value" )
        val serializer = MapAsArraySerializer( String.serializer(), String.serializer() )
        val serialized = json.encodeToString( serializer, map )
        val parsed = json.decodeFromString( serializer, serialized )

        assertEquals( 1, parsed.size )
        assertEquals( "value", parsed[ "key.with.dots" ] )
    }
}
