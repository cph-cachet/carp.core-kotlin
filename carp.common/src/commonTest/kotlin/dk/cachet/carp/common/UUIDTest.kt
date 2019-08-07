package dk.cachet.carp.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.test.*


/**
 * Tests for [UUID].
 */
class UUIDTest
{
    @Test
    fun can_serialize_and_deserialize_UUID_using_JSON()
    {
        val id = UUID( "00000000-0000-0000-0000-00000000000" )

        val json = Json( JsonConfiguration.Stable )
        val serialized = json.stringify( UUIDSerializer, id )
        val parsed = json.parse( UUIDSerializer, serialized )

        assertEquals( id, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_nullable_UUID()
    {
        @Serializable
        data class Id
        (
            @Serializable( with = UUIDSerializer::class )
            val id: UUID?
        )
        val json = Json( JsonConfiguration.Stable )

        val id: Id = Id( UUID( "00000000-0000-0000-0000-00000000000" ) )
        val idSerialized = json.stringify( Id.serializer(), id )
        val idParsed = json.parse( Id.serializer(), idSerialized )
        assertEquals( id, idParsed )

        val nullableId = Id( null )
        val nullableSerialized = json.stringify( Id.serializer(), nullableId )
        val nullableParsed = json.parse( Id.serializer(), nullableSerialized )
        assertEquals( nullableId, nullableParsed )
    }
}