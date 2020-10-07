package dk.cachet.carp.common.data

import kotlin.test.*


/**
 * Tests for [DataType].
 */
class DataTypeTest
{
    @Test
    fun namespace_needs_to_be_set()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataType( "", "typename" )
        }
    }

    @Test
    fun name_may_not_contain_periods()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataType( "some.namespace", "contains.dot" )
        }
    }

    @Test
    fun fromFullyQualifiedName_succeeds()
    {
        val namespace = "some.namespace"
        val name = "typename"
        val type = DataType.fromFullyQualifiedName( "$namespace.$name" )

        assertEquals( namespace, type.namespace )
        assertEquals( name, type.name )
    }

    @Test
    fun fromFullyQualifiedName_fails_without_periods()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataType.fromFullyQualifiedName( "typename" )
        }
    }
}
