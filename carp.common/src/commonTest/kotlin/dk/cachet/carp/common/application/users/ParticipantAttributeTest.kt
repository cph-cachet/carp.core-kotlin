package dk.cachet.carp.common.application.users

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.data.input.elements.InputElement
import dk.cachet.carp.common.application.data.input.elements.Text
import kotlin.reflect.KClass
import kotlin.test.*


/**
 * Tests for [ParticipantAttribute].
 */
class ParticipantAttributeTest
{
    @Test
    fun getInputElement_succeeds_for_registered_type()
    {
        // Create input data type list for testing.
        val type = InputDataType( "test", "test" )
        val inputElement = Text( "Test" )
        val toData = { _: String -> Sex.Male }
        val toInput = { _: Sex -> "Male" }
        val inputDataTypeList =
            object : InputDataTypeList()
            {
                val SEX = add( type, inputElement, Sex::class, toData, toInput )
            }

        val attribute = ParticipantAttribute.DefaultParticipantAttribute( inputDataTypeList.SEX )

        val retrievedInputElement = attribute.getInputElement( inputDataTypeList )
        assertEquals( inputElement, retrievedInputElement )
    }

    @Test
    fun getInputElement_succeeds_for_custom_type()
    {
        val emptyList = object : InputDataTypeList() {}
        val inputElement = Text( "Custom" )
        val attribute = ParticipantAttribute.CustomParticipantAttribute( inputElement )

        val retrievedInputElement = attribute.getInputElement( emptyList )
        assertEquals( inputElement, retrievedInputElement )
    }


    // Helper classes and functions to set up and run test cases.
    private data class AttributeTest<T>( val input: T, val isInputValid: Boolean, val dataIfValid: Data? )
    private data class AttributeWithTest( val attribute: ParticipantAttribute, val test: AttributeTest<*> )
    private fun ParticipantAttribute.expect( vararg tests: AttributeTest<*> ) =
        tests.map { AttributeWithTest( this, it ) }.toTypedArray()
    private fun <T> T.canConvert( isValid: Boolean, data: Data? ) = AttributeTest( this, isValid, data )
    private fun runAttributeTests( block: ParticipantAttribute.(AttributeTest<*>) -> Unit ) =
        tests.forEach { block( it.attribute, it.test ) }

    // Test cases.
    private val tests: Array<AttributeWithTest> = arrayOf(
        // Defined type.
        *ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ).expect(
            null.canConvert( true, null ),
            "Male".canConvert( true, Sex.Male ),
            42.canConvert( false, null ), // Wrong data type.
            "Zorg".canConvert( false, null ) // Breaking constraints.
        ),

        // Custom type.
        *ParticipantAttribute.CustomParticipantAttribute( Text( "Answer" ) ).expect(
            null.canConvert( true, null ),
            "42".canConvert( true, CustomInput( "42" ) ),
            42.canConvert( false, null ) // Wrong data type.
        )
    )

    @Test
    fun isValidInput_test_cases() = runAttributeTests { test ->
        val isValid = isValidInput( CarpInputDataTypes, test.input )
        assertEquals( test.isInputValid, isValid )
    }

    @Test
    fun inputToData_test_cases() = runAttributeTests { test ->
        if ( !test.isInputValid )
        {
            assertFailsWith<IllegalArgumentException> { inputToData( CarpInputDataTypes, test.input ) }
        }
        else assertEquals( test.dataIfValid, inputToData( CarpInputDataTypes, test.input ) )
    }

    @Test
    fun isValidData_test_cases() = runAttributeTests { test ->
        if ( test.isInputValid ) assertTrue( isValidData( CarpInputDataTypes, test.dataIfValid ) )

        val wrongData = object : Data { }
        assertFalse( isValidData( CarpInputDataTypes, wrongData ) )
    }

    @Test
    fun dataToInput_test_cases() = runAttributeTests { test ->
        if ( test.isInputValid ) assertEquals( test.input, dataToInput( CarpInputDataTypes, test.dataIfValid ) )

        val wrongData = object : Data { }
        assertFailsWith<IllegalArgumentException> { dataToInput( CarpInputDataTypes, wrongData ) }
    }

    @Test
    fun dataToInput_may_still_succeed_while_isValidData_is_false()
    {
        val constrainedElement =
            object : InputElement<String>
            {
                override val prompt: String = "Test"
                override fun isValid( input: String ): Boolean = input == "Correct"
                override fun getDataClass(): KClass<String> = String::class
            }
        val attribute = ParticipantAttribute.CustomParticipantAttribute( constrainedElement )
        val emptyList = object : InputDataTypeList() {}

        // Data matching input element constraints.
        val correctData = CustomInput( "Correct" )
        assertEquals( correctData.input, attribute.dataToInput( emptyList, correctData ) )
        assertTrue( attribute.isValidData( emptyList, correctData ) )

        // Data breaking input element constraints.
        val incorrectData = CustomInput( "Incorrect" )
        assertEquals( incorrectData.input, attribute.dataToInput( emptyList, incorrectData ) )
        assertFalse( attribute.isValidData( emptyList, CustomInput( "Incorrect" ) ) )
    }

    @Test
    fun all_functions_fail_for_unregistered_input_data_type()
    {
        val emptyList = object : InputDataTypeList() {}
        val unknownType = InputDataType( "namespace", "unknowntype" )
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( unknownType )

        assertFailsWith<UnsupportedOperationException> { attribute.getInputElement( emptyList ) }
        assertFailsWith<UnsupportedOperationException> { attribute.isValidInput( emptyList, "Test" ) }
        assertFailsWith<UnsupportedOperationException> { attribute.inputToData( emptyList, "Test" ) }
        assertFailsWith<UnsupportedOperationException> { attribute.isValidData( emptyList, CustomInput( "Test" ) ) }
        assertFailsWith<UnsupportedOperationException> { attribute.dataToInput( emptyList, CustomInput( "Test" ) ) }
    }
}
