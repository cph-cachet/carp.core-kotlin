package dk.cachet.carp.common.application

import kotlin.test.*


/**
 * Tests for [NamespacedId].
 */
class NamespacedIdTest
{
    @Test
    fun namespace_needs_to_be_set()
    {
        assertFailsWith<IllegalArgumentException>
        {
            NamespacedId( "", "typename" )
        }
    }

    @Test
    fun name_may_not_contain_periods()
    {
        assertFailsWith<IllegalArgumentException>
        {
            NamespacedId( "some.namespace", "contains.dot" )
        }
    }

    @Test
    fun name_may_not_contain_non_alphanumerics()
    {
        assertFailsWith<IllegalArgumentException>
        {
            NamespacedId( "some.namespace", "contains-hyphen!" )
        }
    }

    @Test
    fun namespace_cannot_end_with_dot()
    {
        assertFailsWith<IllegalArgumentException>
        {
            NamespacedId("some.namespace.", "name")
        }
    }

    @Test
    fun namespace_cannot_start_with_dot()
    {
        assertFailsWith<IllegalArgumentException>
        {
            NamespacedId(".some.namespace", "name")
        }
    }

    @Test
    fun fromConstructor_succeeds()
    {
        val namespace = "some.namespace"
        val name = "typename"
        val fqName = NamespacedId(namespace, name)

        assertEquals( "$namespace.$name", fqName.toString() )
    }

    @Test
    fun fromString_succeeds()
    {
        val namespace = "some.namespace"
        val name = "typename"
        val fqName = NamespacedId.fromString( "$namespace.$name" )

        assertEquals( namespace, fqName.namespace )
        assertEquals( name, fqName.name )
    }

    @Test
    fun fromString_lenient_succeeds()
    {
        val namespace = " some.nameSpace"
        val name = "typeName"
        val fqName = NamespacedId.fromString( "$namespace.$name" )

        assertEquals( "some.namespace", fqName.namespace )
        assertEquals( "typename", fqName.name )
    }

    @Test
    fun fromString_fails_without_periods()
    {
        assertFailsWith<IllegalArgumentException>
        {
            NamespacedId.fromString( "typename" )
        }
    }
}
