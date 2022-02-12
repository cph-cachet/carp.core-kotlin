package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [NotSerializable].
 */
class NotSerializableTest
{
    @Serializable
    sealed class Base<out T>
    {
        object Serializer : KSerializer<Base<*>> by ignoreTypeParameters( ::serializer )
    }

    @Serializable
    data class Extends( val value: Int ) : Base<Unit>()


    @Test
    fun normal_serializer_doesnt_allow_star_projections()
    {
        val value: Base<*> = Extends( 42 )
        assertFailsWith<IllegalArgumentException> { Json.encodeToString( value ) }
    }

    @Test
    fun ignoreTypeParameters_serializer_succeeds()
    {
        val value: Base<*> = Extends( 42 )

        val serialized = Json.encodeToString( Base.Serializer, value )
        val parsed = Json.decodeFromString( Base.Serializer, serialized )

        assertEquals( value, parsed )
    }

    @Test
    fun ignoreTypeParameters_serializer_maintains_configuration()
    {
        val json = Json { useArrayPolymorphism = false }

        val value: Base<*> = Extends( 42 )
        val serialized = json.encodeToString( Base.Serializer, value )

        assertTrue( serialized.startsWith( "{" ) )
    }
}
