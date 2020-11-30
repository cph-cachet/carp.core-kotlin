package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.data.input.InputDataType
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
}
