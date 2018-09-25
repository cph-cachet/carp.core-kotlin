package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.json.JSON
import kotlin.test.*


// TODO: Currently, the following test classes can't be nested classes due to a limitation in PolymorphicSerializer:
//       https://github.com/Kotlin/kotlinx.serialization/issues/127
@KSerializable
internal abstract class BaseClass
@KSerializable
internal class A( val a: String = "a" ) : BaseClass()
{
    companion object {
        init { PolymorphicSerializer.registerSerializer( A::class, "dk.cachet.carp.protocols.domain.serialization.A" ) }
    }
}
@KSerializable
internal class B( val b: String = "b" ) : BaseClass()
{
    companion object {
        init { PolymorphicSerializer.registerSerializer( B::class, "dk.cachet.carp.protocols.domain.serialization   .B" ) }
    }
}
@KSerializable
internal class Composite(
    @KSerializable( PolymorphicArrayListSerializer::class )
    val objects: List<BaseClass> )


/**
 * Tests for [PolymorphicSerializer].
 */
class PolymorphicSerializerTest
{
    @Test
    fun can_serialize_polymorph_object()
    {
        val a = A()
        val aJson = JSON.stringify( PolymorphicSerializer, a )

        assertEquals(
            """["dk.cachet.carp.protocols.domain.serialization.A",{"a":"a"}]""",
            aJson )
    }

    @Test
    fun can_deserialize_polymorph_object()
    {
        val a = A()
        val aJson = JSON.stringify( PolymorphicSerializer, a )
        val aParsed = JSON.parse( PolymorphicSerializer, aJson ) as BaseClass

        assertTrue { aParsed is A }
    }

    @Test
    fun can_serialize_polymorph_collection()
    {
        val composite = Composite( listOf( A(), B() ) )
        val json = JSON.stringify( composite )
        val parsed: Composite = JSON.parse( json )

        assertEquals( 2, parsed.objects.count() )
        assertTrue { parsed.objects[ 0 ] is A }
        assertTrue { parsed.objects[ 1 ] is B }
    }
}