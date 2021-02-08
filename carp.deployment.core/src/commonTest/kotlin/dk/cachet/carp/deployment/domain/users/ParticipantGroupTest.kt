package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.CarpInputDataTypes
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.data.input.Sex
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.ParticipantAttribute
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.createComplexParticipantGroup
import dk.cachet.carp.deployment.domain.studyDeploymentFor
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterDeviceProtocol
import kotlin.test.*

/**
 * Tests for [ParticipantGroup].
 */
class ParticipantGroupTest
{
    @Test
    fun fromDeployment_succeeds()
    {
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
        val expectedData = InputDataType( "some", "type" )
        protocol.addExpectedParticipantData( ParticipantAttribute.DefaultParticipantAttribute( expectedData ) )
        val deployment = StudyDeployment( protocol.getSnapshot() )

        val group = ParticipantGroup.fromDeployment( deployment )

        assertEquals( deployment.id, group.studyDeploymentId )
        assertEquals( mapOf( expectedData to null ), group.data )
        assertEquals( 0, group.participations.size )
        assertEquals( 0, group.consumeEvents().size )
    }

    @Test
    fun creating_ParticipantGroup_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val group = createComplexParticipantGroup()
        val snapshot: ParticipantGroupSnapshot = group.getSnapshot()
        val fromSnapshot = ParticipantGroup.fromSnapshot( snapshot )

        assertEquals( group.creationDate, fromSnapshot.creationDate )
        assertEquals( group.studyDeploymentId, fromSnapshot.studyDeploymentId )
        assertEquals( group.participations, fromSnapshot.participations )
        assertEquals( group.expectedData, fromSnapshot.expectedData )
        assertEquals(
            group.data.map { it.key to it.value }.toSet(),
            fromSnapshot.data.map { it.key to it.value }.toSet()
        )
        assertEquals( 0, fromSnapshot.consumeEvents().size )
    }

    @Test
    fun addParticipation_and_retrieving_it_succeeds()
    {
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
        val deployment = StudyDeployment( protocol.getSnapshot() )
        val group = ParticipantGroup.fromDeployment( deployment )

        val account = Account.withUsernameIdentity( "test" )
        val participation = Participation( group.studyDeploymentId )
        group.addParticipation( account, participation )
        val retrievedParticipation = group.getParticipation( account )

        assertEquals( participation, retrievedParticipation )
        val expectedParticipation = AccountParticipation( account.id, participation.id )
        assertEquals( ParticipantGroup.Event.ParticipationAdded( expectedParticipation ), group.consumeEvents().last() )
    }

    @Test
    fun addParticipation_for_incorrect_study_deployment_fails()
    {
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        val group = ParticipantGroup.fromDeployment( deployment )

        val account = Account.withUsernameIdentity( "test" )
        val incorrectDeploymentId = UUID.randomUUID()
        val participation = Participation( incorrectDeploymentId )
        assertFailsWith<IllegalArgumentException> { group.addParticipation( account, participation ) }
        assertEquals( 0, group.consumeEvents().filterIsInstance<ParticipantGroup.Event.ParticipationAdded>().count() )
    }

    @Test
    fun addParticipation_for_existing_account_fails()
    {
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
        val deployment = StudyDeployment( protocol.getSnapshot() )
        val group = ParticipantGroup.fromDeployment( deployment )
        val account = Account.withUsernameIdentity( "test" )
        group.addParticipation( account, Participation( group.studyDeploymentId ) )

        assertFailsWith<IllegalArgumentException>
        {
            group.addParticipation( account, Participation( group.studyDeploymentId ) )
        }
        assertEquals( 1, group.consumeEvents().filterIsInstance<ParticipantGroup.Event.ParticipationAdded>().count() )
    }

    @Test
    fun getParticipation_for_non_participating_account_returns_null()
    {
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
        val deployment = StudyDeployment( protocol.getSnapshot() )
        val group = ParticipantGroup.fromDeployment( deployment )

        val account = Account.withUsernameIdentity( "test" )
        val participation = group.getParticipation( account )
        assertNull( participation )
    }

    @Test
    fun setData_succeeds()
    {
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
        protocol.addExpectedParticipantData( ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ) )
        val deployment = StudyDeployment( protocol.getSnapshot() )

        val group = ParticipantGroup.fromDeployment( deployment )

        group.setData( CarpInputDataTypes, CarpInputDataTypes.SEX, Sex.Male )
        assertEquals( Sex.Male, group.data[ CarpInputDataTypes.SEX ] )
    }

    @Test
    fun setData_fails_for_unexpected_data()
    {
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
        val deployment = StudyDeployment( protocol.getSnapshot() )
        val group = ParticipantGroup.fromDeployment( deployment )

        assertFailsWith<IllegalArgumentException>
        {
            group.setData( CarpInputDataTypes, CarpInputDataTypes.SEX, Sex.Male )
        }
    }

    @Test
    fun setData_fails_for_invalid_data()
    {
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
        protocol.addExpectedParticipantData( ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ) )
        val deployment = StudyDeployment( protocol.getSnapshot() )
        val group = ParticipantGroup.fromDeployment( deployment )

        val wrongData = object : Data { }
        assertFailsWith<IllegalArgumentException>
        {
            group.setData( CarpInputDataTypes, CarpInputDataTypes.SEX, wrongData )
        }
    }
}
