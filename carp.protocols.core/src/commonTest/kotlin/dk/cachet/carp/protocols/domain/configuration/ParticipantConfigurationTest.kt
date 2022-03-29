package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.protocols.application.users.ExpectedParticipantData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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
        val expectedData = ExpectedParticipantData( attribute )
        val isAdded = configuration.addExpectedParticipantData( expectedData )

        assertTrue( isAdded )
        assertEquals( 1, configuration.expectedParticipantData.size )
        assertEquals( expectedData, configuration.expectedParticipantData.single() )
    }

    @Test
    fun addExpectedParticipantData_ignores_duplicates()
    {
        val configuration = createParticipantConfiguration()
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        val inputBy = ExpectedParticipantData.InputBy.Roles( setOf( "Patient" ) )
        val expectedData = ExpectedParticipantData( attribute, inputBy )
        configuration.addExpectedParticipantData( expectedData )

        val isAdded = configuration.addExpectedParticipantData( expectedData )

        assertFalse( isAdded )
        assertEquals( 1, configuration.expectedParticipantData.size )
    }

    @Test
    fun addExpectedParticipantData_fails_for_conflicting_participant_attribute()
    {
        val configuration = createParticipantConfiguration()
        val prompt = "Test"
        val attribute = ParticipantAttribute.CustomParticipantAttribute( Text( prompt ) )
        val expectedData = ExpectedParticipantData( attribute )
        configuration.addExpectedParticipantData( expectedData )

        // The API prevents users from creating conflicting attributes, but they can be created through JSON.
        // Modify "prompt" in previously added attribute, but reuse same `InputDataType`.
        val json = createTestJSON()
        val conflictingJson = json.encodeToString( expectedData ).replace( prompt, "Changed!" )
        val conflictingExpectedData: ExpectedParticipantData = json.decodeFromString( conflictingJson )

        assertFailsWith<IllegalArgumentException>
        {
            configuration.addExpectedParticipantData( conflictingExpectedData )
        }
    }

    @Test
    fun removeExpectedParticipantData_succeeds()
    {
        val configuration = createParticipantConfiguration()
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        val expectedData = ExpectedParticipantData( attribute )
        configuration.addExpectedParticipantData( expectedData )

        val isRemoved = configuration.removeExpectedParticipantData( expectedData )

        assertTrue( isRemoved )
        assertEquals( 0, configuration.expectedParticipantData.size )
    }
}
