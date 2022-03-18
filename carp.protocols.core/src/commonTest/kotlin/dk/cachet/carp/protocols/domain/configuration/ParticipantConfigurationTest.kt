package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import kotlin.test.*


/**
 * Base class with tests for [ParticipantConfiguration] which can be used to test extending types.
 */
interface ParticipantConfigurationTest
{
    /**
     * Called for each test to create a participant configuration to run tests on.
     */
    fun createParticipantConfiguration(): ParticipantConfiguration


    @Test
    fun addParticipantRole_succeeds()
    {
        val configuration = createParticipantConfiguration()

        val role = ParticipantRole( "Participant", false )
        val isAdded = configuration.addParticipantRole( role )

        assertTrue( isAdded )
        assertEquals( 1, configuration.participantRoles.size )
        assertEquals( role, configuration.participantRoles.single() )
    }

    @Test
    fun addParticipantRole_ignores_duplicates()
    {
        val configuration = createParticipantConfiguration()
        val role = ParticipantRole( "Participant", false )
        configuration.addParticipantRole( role )

        val isAdded = configuration.addParticipantRole( role )

        assertFalse( isAdded )
        assertEquals( 1, configuration.participantRoles.size )
    }

    @Test
    fun removeParticipantRole_succeeds()
    {
        val configuration = createParticipantConfiguration()
        val role = ParticipantRole( "Participant", false )
        configuration.addParticipantRole( role )

        val isRemoved = configuration.removeParticipantRole( role )

        assertTrue( isRemoved )
        assertEquals( 0, configuration.participantRoles.size )
    }

    @Test
    fun removeParticipantRole_returns_false_for_nonexisting_roles()
    {
        val configuration = createParticipantConfiguration()
        configuration.addParticipantRole( ParticipantRole( "Participant", false ))

        val nonExistingRole = ParticipantRole( "Other participant", false )
        val isRemoved = configuration.removeParticipantRole( nonExistingRole )

        assertFalse( isRemoved )
    }

    @Test
    fun addExpectedParticipantData_succeeds()
    {
        val configuration = createParticipantConfiguration()

        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        val isAdded = configuration.addExpectedParticipantData( attribute )

        assertTrue( isAdded )
        assertEquals( 1, configuration.expectedParticipantData.size )
        assertEquals( attribute, configuration.expectedParticipantData.single() )
    }

    @Test
    fun addExpectedParticipantData_ignores_duplicates()
    {
        val configuration = createParticipantConfiguration()
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        configuration.addExpectedParticipantData( attribute )

        val isAdded = configuration.addExpectedParticipantData( attribute )

        assertFalse( isAdded )
        assertEquals( 1, configuration.expectedParticipantData.size )
    }

    @Test
    fun removeExpectedParticipantData_succeeds()
    {
        val configuration = createParticipantConfiguration()
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        configuration.addExpectedParticipantData( attribute )

        val isRemoved = configuration.removeExpectedParticipantData( attribute )

        assertTrue( isRemoved )
        assertEquals( 0, configuration.expectedParticipantData.size )
    }

    @Test
    fun isValidParticipantData_matches_isValidData_for_expected_data()
    {
        val defaultAttribute = ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        val customAttribute = ParticipantAttribute.CustomParticipantAttribute( Text( "Test " ) )
        val attributes = setOf( defaultAttribute, customAttribute )

        // Default (registered) types.
        assertEquals(
            defaultAttribute.isValidData( CarpInputDataTypes, Sex.Male ),
            attributes.isValidParticipantData( CarpInputDataTypes, defaultAttribute.inputDataType, Sex.Male )
        )
        assertEquals(
            defaultAttribute.isValidData( CarpInputDataTypes, CustomInput( "Zorg" ) ),
            attributes.isValidParticipantData( CarpInputDataTypes, defaultAttribute.inputDataType, CustomInput( "Zorg" ) )
        )

        // Custom types.
        assertEquals(
            customAttribute.isValidData( CarpInputDataTypes, CustomInput( "Valid" ) ),
            attributes.isValidParticipantData( CarpInputDataTypes, customAttribute.inputDataType, CustomInput( "Valid" ) )
        )
        assertEquals(
            customAttribute.isValidData( CarpInputDataTypes, CustomInput( -1 ) ),
            attributes.isValidParticipantData( CarpInputDataTypes, customAttribute.inputDataType, CustomInput( -1 ) )
        )
    }

    @Test
    fun isValidParticipantData_returns_false_for_unexpected_data()
    {
        val unexpectedType = CarpInputDataTypes.SEX
        val isValid = emptySet<ParticipantAttribute>().isValidParticipantData( CarpInputDataTypes, unexpectedType, Sex.Male )
        assertFalse( isValid )
    }
}
