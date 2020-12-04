package dk.cachet.carp.common.data.input

import dk.cachet.carp.common.data.input.element.Text
import kotlin.test.*


/**
 * Tests for [InputDataTypeList].
 */
class InputDataTypeListTest
{
    @Test
    fun add_succeeds()
    {
        val type = InputDataType( "test", "test" )
        val inputElement = Text( "Test" )
        val toData = { _: String -> Sex.Male }
        val toInput = { _: Sex -> "Male" }

        val list =
            object : InputDataTypeList()
            {
                val TEST = add( type, inputElement, Sex::class, toData, toInput )
            }

        // Added item can be queried.
        assertEquals( type, list.single() )

        // Input element and data converter have been added.
        assertEquals( inputElement, list.inputElements[ list.TEST ] )
        assertEquals( Sex::class, list.dataClasses[ list.TEST ] )
        assertEquals<Any?>( toData, list.inputToDataConverters[ list.TEST ] )
        assertEquals<Any?>( toInput, list.dataToInputConverters[ list.TEST ] )
    }

    @Test
    fun add_fails_for_already_registered_types()
    {
        val type = InputDataType( "test", "test" )
        val inputElement = Text( "Test" )
        val toData = { _: String -> Sex.Male }
        val toInput = { _: Sex -> "Male" }

        assertFailsWith<IllegalArgumentException>
        {
            object : InputDataTypeList()
            {
                val TEST = add( type, inputElement, Sex::class, toData, toInput )
                val TEST2 = add( type, inputElement, Sex::class, toData, toInput )
            }
        }
    }
}
