package dk.cachet.carp.protocols.application.users

import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.users.ParticipantAttribute
import kotlin.test.*


/**
 * Tests for [ExpectedParticipantData].
 */
class ExpectedParticipantDataTest
{
    @Test
    fun isValidParticipantData_matches_isValidData_for_expected_data()
    {
        val defaultExpectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        )
        val customExpectedData = ExpectedParticipantData(
            ParticipantAttribute.CustomParticipantAttribute( Text( "Test " ) )
        )
        val expectedData = setOf( defaultExpectedData, customExpectedData )

        // Default (registered) types.
        assertEquals(
            defaultExpectedData.attribute.isValidData( CarpInputDataTypes, Sex.Male ),
            expectedData.isValidParticipantData( CarpInputDataTypes, defaultExpectedData.inputDataType, Sex.Male )
        )
        assertEquals(
            defaultExpectedData.attribute.isValidData( CarpInputDataTypes, CustomInput( "Zorg" ) ),
            expectedData.isValidParticipantData( CarpInputDataTypes, defaultExpectedData.inputDataType, CustomInput( "Zorg" ) )
        )

        // Custom types.
        assertEquals(
            customExpectedData.attribute.isValidData( CarpInputDataTypes, CustomInput( "Valid" ) ),
            expectedData.isValidParticipantData( CarpInputDataTypes, customExpectedData.inputDataType, CustomInput( "Valid" ) )
        )
        assertEquals(
            customExpectedData.attribute.isValidData( CarpInputDataTypes, CustomInput( -1 ) ),
            expectedData.isValidParticipantData( CarpInputDataTypes, customExpectedData.inputDataType, CustomInput( -1 ) )
        )
    }

    @Test
    fun isValidParticipantData_returns_false_for_unexpected_data()
    {
        val unexpectedType = CarpInputDataTypes.SEX
        val isValid = emptySet<ExpectedParticipantData>().isValidParticipantData( CarpInputDataTypes, unexpectedType, Sex.Male )
        assertFalse( isValid )
    }

    @Test
    fun isValidParticipantData_returns_true_when_data_can_be_set_by_anyone()
    {
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ),
            ExpectedParticipantData.InputBy.Anyone
        )

        val isValid = setOf( expectedData )
            .isValidParticipantData( CarpInputDataTypes, expectedData.inputDataType, Sex.Male, "Participant" )
        assertTrue( isValid )
    }

    @Test
    fun isValidParticipantData_only_true_for_expected_participant_roles()
    {
        val participantRoles = setOf( "Patient", "Caretaker" )
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ),
            ExpectedParticipantData.InputBy.Roles( participantRoles )
        )
        val inputDataType = expectedData.inputDataType
        val allData = setOf( expectedData )

        assertTrue( allData.isValidParticipantData( CarpInputDataTypes, inputDataType, Sex.Male, "Patient" ) )
        assertTrue( allData.isValidParticipantData( CarpInputDataTypes, inputDataType, Sex.Male, "Caretaker" ) )
        assertFalse( allData.isValidParticipantData( CarpInputDataTypes, inputDataType, Sex.Male, "Doctor" ) )
    }
}
