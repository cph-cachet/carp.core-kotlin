package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.CarpInputDataTypes
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.data.input.Sex
import dk.cachet.carp.common.users.ParticipantAttribute
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.createComplexParticipantGroup
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
        assertEquals( group.expectedData, fromSnapshot.expectedData )
        assertEquals(
            group.data.map { it.key to it.value }.toSet(),
            fromSnapshot.data.map { it.key to it.value }.toSet()
        )
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
