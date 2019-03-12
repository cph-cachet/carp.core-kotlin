package dk.cachet.carp.common.serialization

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [PolymorphicSerializer].
 */
class PolymorphicSerializerTest
{
    @Serializable
    internal abstract class BaseClass
    {
        val baseField: Boolean = true
    }
    @Serializable
    internal class A( val a: String = "a" ) : BaseClass()
    {
        companion object
        {
            init { PolymorphicSerializer.registerSerializer( A::class, A.serializer(),"dk.cachet.carp.common.serialization.PolymorphicSerializerTest.A" ) }
        }
    }
    @Serializable
    internal class B( val b: String = "b" ) : BaseClass()
    {
        companion object
        {
            init { PolymorphicSerializer.registerSerializer( B::class, B.serializer(),"dk.cachet.carp.common.serialization.PolymorphicSerializerTest.B" ) }
        }
    }
    @Serializable
    internal class Unregistered : BaseClass()

    @Test
    fun can_serialize_polymorph_object()
    {
        val a = A()
        val aJson = Json.stringify( PolymorphicSerializer, a )

        assertEquals(
            """["dk.cachet.carp.common.serialization.PolymorphicSerializerTest.A",{"baseField":true,"a":"a"}]""",
            aJson )
    }

    @Test
    fun can_deserialize_polymorph_object()
    {
        val a = A()
        val aJson = Json.stringify( PolymorphicSerializer, a )
        val aParsed = Json.parse( PolymorphicSerializer, aJson ) as BaseClass

        assertTrue { aParsed is A }
        assertTrue { aParsed.baseField }
    }

    @Test
    fun unregistered_type_triggers_exception()
    {
        val unregistered = Unregistered()

        assertFailsWith<NoSuchElementException>
        {
            Json.stringify( PolymorphicSerializer, unregistered )
        }
    }


    internal class DuplicateClassName
    {
        @Serializable
        class A
    }

    @Test
    fun cant_register_duplicate_class_names()
    {
        assertFailsWith<IllegalArgumentException>
        {
            PolymorphicSerializer.registerSerializer(
                DuplicateClassName.A::class,
                DuplicateClassName.A.serializer(),
                "dk.cachet.carp.common.serialization.PolymorphicSerializerTest.DuplicateClassName.A" )
        }
    }


    @Serializable
    internal class PolymorphicList(
        @Serializable( PolymorphicArrayListSerializer::class )
        val objects: List<BaseClass> )

    @Test
    fun can_serialize_and_deserialize_polymorph_list()
    {
        val list = PolymorphicList( listOf( A(), B() ) )
        val serializer = PolymorphicList.serializer()
        val json = Json.stringify( serializer, list )
        val parsed: PolymorphicList = Json.parse( serializer, json )

        assertEquals( 2, parsed.objects.count() )
        assertTrue { parsed.objects[ 0 ] is A }
        assertTrue { parsed.objects[ 1 ] is B }
    }


    @Serializable
    internal abstract class AbstractTopClass
    {
        @Transient
        abstract val nested: List<AbstractNested>
    }
    @Serializable
    internal class TopClass(
        @Serializable( PolymorphicArrayListSerializer::class )
        override val nested: List<AbstractNested> ) : AbstractTopClass()
    @Serializable
    internal abstract class AbstractNested
    {
        @Transient
        abstract val field: BaseClass
    }
    @Serializable
    internal class Nested(
        @Serializable( PolymorphicSerializer::class )
        override val field: BaseClass ) : AbstractNested()
    {
        companion object
        {
            init { PolymorphicSerializer.registerSerializer( Nested::class, Nested.serializer(), "dk.cachet.carp.common.serialization.PolymorphicSerializerTest.Nested" ) }
        }
    }

    @Test
    fun can_serialize_and_deserialize_nested_polymorph_object()
    {
        val top = TopClass( listOf( Nested(A()), Nested(B()) ) )
        val serializer = TopClass.serializer()
        val json = Json.stringify( serializer, top )
        val parsed: TopClass = Json.parse( serializer, json )

        assertEquals( 2, parsed.nested.count() )
        assertTrue { parsed.nested[ 0 ].field is A }
        assertTrue { parsed.nested[ 1 ].field is B }
    }
}