package dk.cachet.carp.common.users

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
        val inputTypeList = object : InputDataTypeList()
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

    @Test
    fun inputToData_succeeds_for_defined_types()
    {
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        val data = attribute.inputToData( CarpInputDataTypes, "Male" )
        assertEquals( Sex.Male, data )
    }

    @Test
    fun inputToData_succeeds_for_custom_types()
    {
        val attribute = ParticipantAttribute.CustomParticipantAttribute( Text( "Answer" ) )
        val data = attribute.inputToData( CarpInputDataTypes, "42" )
        assertTrue( data is CustomInput<*> )
        assertEquals( "42", data.input )
    }

    @Test
    fun inputToData_fails_for_invalid_input_type()
    {
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        assertFailsWith<IllegalArgumentException> { attribute.inputToData( CarpInputDataTypes, 42 ) }
    }

    @Test
    fun inputToData_fails_when_breaking_constraints()
    {
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        assertFailsWith<IllegalArgumentException> { attribute.inputToData( CarpInputDataTypes, "Zorg" ) }
    }

    @Test
    fun inputToData_fails_for_unsupported_input_data_type()
    {
        val unknownType = InputDataType( "namespace", "unknowntype" )
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( unknownType )
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
