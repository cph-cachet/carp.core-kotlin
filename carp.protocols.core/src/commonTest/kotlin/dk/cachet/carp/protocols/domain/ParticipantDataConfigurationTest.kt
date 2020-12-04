package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.data.input.CarpInputDataTypes
import dk.cachet.carp.common.data.input.CustomInput
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.data.input.Sex
import dk.cachet.carp.common.data.input.element.Text
import dk.cachet.carp.common.users.ParticipantAttribute
import kotlin.test.*


/**
 * Base class with tests for [ParticipantDataConfiguration] which can be used to test extending types.
 */
interface ParticipantDataConfigurationTest
{
    /**
     * Called for each test to create a participant data configuration to run tests on.
     */
    fun createParticipantDataConfiguration(): ParticipantDataConfiguration


    @Test
    fun addExpectedParticipantData_succeeds()
    {
        val configuration = createParticipantDataConfiguration()

        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        val isAdded = configuration.addExpectedParticipantData( attribute )

        assertTrue( isAdded )
        assertEquals( 1, configuration.expectedParticipantData.size )
        assertEquals( attribute, configuration.expectedParticipantData.single() )
    }

    @Test
    fun addExpectedParticipantData_ignores_duplicates()
    {
        val configuration = createParticipantDataConfiguration()
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        configuration.addExpectedParticipantData( attribute )

        val isAdded = configuration.addExpectedParticipantData( attribute )

        assertFalse( isAdded )
        assertEquals( 1, configuration.expectedParticipantData.size )
    }

    @Test
    fun removeExpectedParticipantData_succeeds()
    {
        val configuration = createParticipantDataConfiguration()
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        configuration.addExpectedParticipantData( attribute )

        val isRemoved = configuration.removeExpectedParticipantData( attribute )

        assertTrue( isRemoved )
        assertEquals( 0, configuration.expectedParticipantData.size )
    }

    @Test
    fun isValidParticipantData_matches_isValidData_for_expected_data()
    {
        val configuration = createParticipantDataConfiguration()
        val defaultAttribute = ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        configuration.addExpectedParticipantData( defaultAttribute )
        val customAttribute = ParticipantAttribute.CustomParticipantAttribute( Text( "Test " ) )
        configuration.addExpectedParticipantData( customAttribute )

        // Default (registered) types.
        assertEquals(
            defaultAttribute.isValidData( CarpInputDataTypes, Sex.Male ),
            configuration.isValidParticipantData( CarpInputDataTypes, defaultAttribute.inputType, Sex.Male )
        )
        assertEquals(
            defaultAttribute.isValidData( CarpInputDataTypes, CustomInput( "Zorg" ) ),
            configuration.isValidParticipantData( CarpInputDataTypes, defaultAttribute.inputType, CustomInput( "Zorg" ) )
        )

        // Custom types.
        assertEquals(
            customAttribute.isValidData( CarpInputDataTypes, CustomInput( "Valid" ) ),
            configuration.isValidParticipantData( CarpInputDataTypes, customAttribute.inputType, CustomInput( "Valid" ) )
        )
        assertEquals(
            customAttribute.isValidData( CarpInputDataTypes, CustomInput( -1 ) ),
            configuration.isValidParticipantData( CarpInputDataTypes, customAttribute.inputType, CustomInput( -1 ) )
        )
    }

    @Test
    fun isValidParticipantData_returns_false_for_unexpected_data()
    {
        val configuration = createParticipantDataConfiguration()

        val unexpectedType = CarpInputDataTypes.SEX
        val isValid = configuration.isValidParticipantData( CarpInputDataTypes, unexpectedType, Sex.Male )
        assertFalse( isValid )
    }
}
