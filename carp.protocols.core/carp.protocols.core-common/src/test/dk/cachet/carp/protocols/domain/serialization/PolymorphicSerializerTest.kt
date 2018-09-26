package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.json.JSON
import kotlin.test.*


// TODO: Currently, the following test classes can't be nested classes due to a limitation in PolymorphicSerializer:
//       https://github.com/Kotlin/kotlinx.serialization/issues/127

@KSerializable
internal abstract class BaseClass
{
    val baseField: Boolean = true
}
@KSerializable
internal class A( val a: String = "a" ) : BaseClass()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( A::class, "dk.cachet.carp.protocols.domain.serialization.A" ) }
    }
}
@KSerializable
internal class B( val b: String = "b" ) : BaseClass()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( B::class, "dk.cachet.carp.protocols.domain.serialization.B" ) }
    }
}
@KSerializable
internal class Unregistered : BaseClass()


@KSerializable
internal class PolymorphicList(
    @KSerializable( PolymorphicArrayListSerializer::class )
    val objects: List<BaseClass> )


@KSerializable
internal abstract class AbstractTopClass
{
    @Transient
    abstract val nested: List<AbstractNested>
}
@KSerializable
internal class TopClass(
    @KSerializable( PolymorphicArrayListSerializer::class )
    override val nested: List<AbstractNested> ) : AbstractTopClass()
@KSerializable
internal abstract class AbstractNested
{
    @Transient
    abstract val field: BaseClass
}
@KSerializable
internal class Nested(
    @KSerializable( PolymorphicSerializer::class )
    override val field: BaseClass ) : AbstractNested()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( Nested::class, "dk.cachet.carp.protocols.domain.serialization.Nested" ) }
    }
}


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
            """["dk.cachet.carp.protocols.domain.serialization.A",{"baseField":true,"a":"a"}]""",
            aJson )
    }

    @Test
    fun can_deserialize_polymorph_object()
    {
        val a = A()
        val aJson = JSON.stringify( PolymorphicSerializer, a )
        val aParsed = JSON.parse( PolymorphicSerializer, aJson ) as BaseClass

        assertTrue { aParsed is A }
        assertTrue { aParsed.baseField }
    }

    @Test
    fun unregistered_type_triggers_exception()
    {
        val unregistered = Unregistered()

        assertFailsWith<NoSuchElementException>
        {
            JSON.stringify( PolymorphicSerializer, unregistered )
        }
    }

    @Test
    fun can_serialize_and_deserialize_polymorph_list()
    {
        val list = PolymorphicList( listOf( A(), B() ) )
        val json = JSON.stringify( list )
        val parsed: PolymorphicList = JSON.parse( json )

        assertEquals( 2, parsed.objects.count() )
        assertTrue { parsed.objects[ 0 ] is A }
        assertTrue { parsed.objects[ 1 ] is B }
    }

    @Test
    fun can_serialize_and_deserialize_nested_polymorph_object()
    {
        val top = TopClass( listOf( Nested(A()), Nested(B()) ) )
        val json = JSON.stringify( top )
        val parsed: TopClass = JSON.parse( json )

        assertEquals( 2, parsed.nested.count() )
        assertTrue { parsed.nested[ 0 ].field is A }
        assertTrue { parsed.nested[ 1 ].field is B }
    }
}