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
        val dataConverter = { _: String -> Sex.Male }

        val list = object : InputDataTypeList()
        {
            val TEST = add( type, inputElement, dataConverter )
        }

        // Added item can be queried.
        assertEquals( type, list.single() )

        // Input element and data converter have been added.
        assertEquals( inputElement, list.inputElements[ list.TEST ] )
        @Suppress( "TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING" )
        assertEquals( dataConverter, list.dataConverters[ list.TEST ] )
    }

    @Test
    fun add_fails_for_already_registered_types()
    {
        val type = InputDataType( "test", "test" )
        val inputElement = Text( "Test" )
        val dataConverter = { _: String -> Sex.Male }

        assertFailsWith<IllegalArgumentException>
        {
            object : InputDataTypeList()
            {
                val TEST = add( type, inputElement, dataConverter )
                val TEST2 = add( type, inputElement, dataConverter )
            }
        }
    }
}
