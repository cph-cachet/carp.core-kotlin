package dk.cachet.carp.common

import kotlin.test.*


/**
 * Tests for [FullyQualifiedName].
 */
class FullyQualifiedNameTest
{
    @Test
    fun namespace_needs_to_be_set()
    {
        assertFailsWith<IllegalArgumentException>
        {
            FullyQualifiedName( "", "typename" )
        }
    }

    @Test
    fun name_may_not_contain_periods()
    {
        assertFailsWith<IllegalArgumentException>
        {
            FullyQualifiedName( "some.namespace", "contains.dot" )
        }
    }

    @Test
    fun fromString_succeeds()
    {
        val namespace = "some.namespace"
        val name = "typename"
        val fqName = FullyQualifiedName.fromString( "$namespace.$name" )

        assertEquals( namespace, fqName.namespace )
        assertEquals( name, fqName.name )
    }

    @Test
    fun fromString_fails_without_periods()
    {
        assertFailsWith<IllegalArgumentException>
        {
            FullyQualifiedName.fromString( "typename" )
        }
    }
}
