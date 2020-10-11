package dk.cachet.carp.common.data

import dk.cachet.carp.common.FullyQualifiedName
import kotlin.test.*


/**
 * Tests for [DataType].
 */
class DataTypeTest
{
    @Test
    fun fromFullyQualifiedName_succeeds()
    {
        val namespace = "some.namespace"
        val name = "typename"
        val type = DataType.fromFullyQualifiedName( "$namespace.$name" )

        assertEquals( FullyQualifiedName( namespace, name ), type.name )
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
