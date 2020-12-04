package dk.cachet.carp.common.users

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.CarpInputDataTypes
import dk.cachet.carp.common.data.input.CustomInput
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.data.input.InputDataTypeList
import dk.cachet.carp.common.data.input.Sex
import dk.cachet.carp.common.data.input.element.Text
import dk.cachet.carp.common.serialization.createDefaultJSON
import kotlin.test.*


/**
 * Tests for [ParticipantAttribute].
 */
class ParticipantAttributeTest
{
    @Test
    fun getInputElement_succeeds_for_registered_type()
    {
        // Create input type list for testing.
        val type = InputDataType( "test", "test" )
        val inputElement = Text( "Test" )
        val dataConverter = { _: String -> Sex.Male }
        val inputTypeList =
            object : InputDataTypeList()
            {
                val SEX = add( type, inputElement, dataConverter )
            }

        val attribute = ParticipantAttribute.DefaultParticipantAttribute( type )

        val retrievedInputElement = attribute.getInputElement( inputTypeList )
        assertEquals( inputElement, retrievedInputElement )
    }

    @Test
    fun getInputElement_fails_for_unregistered_type()
    {
        val emptyList = object : InputDataTypeList() {}
        val unregisteredType = InputDataType( "test", "test" )
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( unregisteredType )

        assertFailsWith<UnsupportedOperationException> { attribute.getInputElement( emptyList ) }
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


    // Helper classes and functions to set up test cases.
    private data class AttributeTest<T>( val input: T, val expectValid: Boolean, val expectData: Data? )
    private data class AttributeWithTest( val attribute: ParticipantAttribute, val test: AttributeTest<*> )
    private fun ParticipantAttribute.expect( vararg tests: AttributeTest<*> ) =
        tests.map { AttributeWithTest( this, it ) }.toTypedArray()
    private fun <T> T.canConvert( isValid: Boolean, data: Data? ) = AttributeTest( this, isValid, data )

    // Test cases.
    private val tests: Array<AttributeWithTest> = arrayOf(
        // Defined type.
        *ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ).expect(
            "Male".canConvert( true, Sex.Male ),
            42.canConvert( false, null ), // Wrong data type.
            "Zorg".canConvert( false, null ) // Breaking constraints.
        ),

        // Custom type.
        *ParticipantAttribute.CustomParticipantAttribute( Text( "Answer" ) ).expect(
            "42".canConvert( true, CustomInput( "42" ) ),
            42.canConvert( false, null ) // Wrong data type.
        )
    )

    @Test
    fun isValid_returns_expected_results_for_all_test_cases()
    {
        for ( (attribute, test) in tests )
        {
            val isValid = attribute.isValid( CarpInputDataTypes, test.input )
            assertEquals( test.expectValid, isValid )
        }
    }

    @Test
    fun inputToData_returns_expected_results_for_all_test_cases()
    {
        for ( (attribute, test) in tests )
        {
            if ( !test.expectValid )
            {
                assertFailsWith<IllegalArgumentException> { attribute.inputToData( CarpInputDataTypes, test.input ) }
            }
            else
            {
                assertEquals( test.expectData, attribute.inputToData( CarpInputDataTypes, test.input ) )
            }
        }
    }

    @Test
    fun isValid_and_inputToData_fail_for_unsupported_input_data_type()
    {
        val unknownType = InputDataType( "namespace", "unknowntype" )
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( unknownType )

        assertFailsWith<UnsupportedOperationException> { attribute.isValid( CarpInputDataTypes, "Test" ) }
        assertFailsWith<UnsupportedOperationException> { attribute.inputToData( CarpInputDataTypes, "Test" ) }
    }

    @Test
    fun can_serialize_and_deserialize_DefaultParticipantAttribute_polymorphic()
    {
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "namespace", "test" ) )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( ParticipantAttribute.serializer(), attribute )
        val parsed = json.decodeFromString( ParticipantAttribute.serializer(), serialized )

        assertEquals( attribute, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_CustomParticipantAttribute_polymorphic()
    {
        val attribute = ParticipantAttribute.CustomParticipantAttribute( Text( "Favorite movie" ) )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( ParticipantAttribute.serializer(), attribute )
        val parsed = json.decodeFromString( ParticipantAttribute.serializer(), serialized )

        assertEquals( attribute, parsed )
    }
}
