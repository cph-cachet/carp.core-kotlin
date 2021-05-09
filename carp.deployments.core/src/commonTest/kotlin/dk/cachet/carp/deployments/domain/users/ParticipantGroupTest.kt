package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.deployments.application.users.AssignedMasterDevice
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.deployments.domain.createComplexParticipantGroup
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

        val group = ParticipantGroup.fromNewDeployment( deployment )

        assertEquals( deployment.id, group.studyDeploymentId )
        val expectedAssignedMasterDevices = protocol.masterDevices
            .map { AssignedMasterDevice( it, null ) }
            .toSet()
        assertEquals( expectedAssignedMasterDevices, group.assignedMasterDevices )
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
        assertEquals( group.assignedMasterDevices, fromSnapshot.assignedMasterDevices )
        assertEquals( group.isStudyDeploymentStopped, fromSnapshot.isStudyDeploymentStopped )
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
        val group = createParticipantGroup()
        val devicesToAssign = group.assignedMasterDevices.map { it.device }.toSet()

        val externalParticipantId = UUID.randomUUID()
        val account = Account.withUsernameIdentity( "test" )
        val participation = Participation( group.studyDeploymentId )
        val invitation = StudyInvitation.empty()
        group.addParticipation( externalParticipantId, account, participation, invitation, devicesToAssign )
        val retrievedParticipation = group.getParticipation( externalParticipantId )

        assertEquals( participation, retrievedParticipation )
        val expectedParticipation = AccountParticipation(
            externalParticipantId,
            account.id,
            participation,
            invitation,
            devicesToAssign.map { it.roleName }.toSet() )
        assertEquals( ParticipantGroup.Event.ParticipationAdded( expectedParticipation ), group.consumeEvents().last() )
    }

    @Test
    fun addParticipation_for_incorrect_study_deployment_fails()
    {
        val group = createParticipantGroup()
        val devicesToAssign = group.assignedMasterDevices.map { it.device }.toSet()

        val account = Account.withUsernameIdentity( "test" )
        val incorrectDeploymentId = UUID.randomUUID()
        val participation = Participation( incorrectDeploymentId )
        assertFailsWith<IllegalArgumentException>
        {
            group.addParticipation(
                externalParticipantId = UUID.randomUUID(),
                account,
                participation,
                StudyInvitation.empty(),
                devicesToAssign )
        }
        assertEquals( 0, group.consumeEvents().filterIsInstance<ParticipantGroup.Event.ParticipationAdded>().count() )
    }

    @Test
    fun addParticipation_for_existing_externalParticipantId_fails()
    {
        val group = createParticipantGroup()
        val devicesToAssign = group.assignedMasterDevices.map { it.device }.toSet()
        val externalParticipantId = UUID.randomUUID()
        val account = Account.withUsernameIdentity( "test" )
        val invitation = StudyInvitation.empty()
        group.addParticipation( externalParticipantId, account, Participation( group.studyDeploymentId ), invitation, devicesToAssign )

        assertFailsWith<IllegalArgumentException>
        {
            group.addParticipation( externalParticipantId, account, Participation( group.studyDeploymentId ), invitation, devicesToAssign )
        }
        assertEquals( 1, group.consumeEvents().filterIsInstance<ParticipantGroup.Event.ParticipationAdded>().count() )
    }

    @Test
    fun addParticipation_fails_when_deployment_stopped()
    {
        val group = createParticipantGroup()
        val devicesToAssign = group.assignedMasterDevices.map { it.device }.toSet()
        group.studyDeploymentStopped()

        val account = Account.withUsernameIdentity( "test" )
        assertFailsWith<IllegalStateException>
        {
            group.addParticipation(
                externalParticipantId = UUID.randomUUID(),
                account,
                Participation( group.studyDeploymentId ),
                StudyInvitation.empty(),
                devicesToAssign )
        }
    }

    @Test
    fun getParticipation_for_nonexisting_external_participant_id_returns_null()
    {
        val group = createParticipantGroup()

        val unknownId = UUID.randomUUID()
        val participation = group.getParticipation( unknownId )
        assertNull( participation )
    }

    @Test
    fun getAssignedMasterDevice_succeeds()
    {
        val masterDeviceRoleName = "Master"
        val protocol = createSingleMasterDeviceProtocol( masterDeviceRoleName )
        val group = createParticipantGroup( protocol )

        val assignedDevice = group.getAssignedMasterDevice( masterDeviceRoleName )
        assertEquals( protocol.masterDevices.single(), assignedDevice.device )
    }

    @Test
    fun getAssignedMasterDevice_fails_for_unknown_device()
    {
        val group = createParticipantGroup()

        assertFailsWith<IllegalArgumentException> { group.getAssignedMasterDevice( "Unknown" ) }
    }

    @Test
    fun updateDeviceRegistration_succeeds()
    {
        val protocol = createSingleMasterDeviceProtocol()
        val device = protocol.masterDevices.first()
        val group = createParticipantGroup( protocol )

        val registration = device.createRegistration()
        group.updateDeviceRegistration( device, registration )
        val assignedDevice = group.assignedMasterDevices.firstOrNull { it.device == device }

        assertEquals( registration, assignedDevice?.registration )
        val registrationEvent = group.consumeEvents().filterIsInstance<ParticipantGroup.Event.DeviceRegistrationChanged>().singleOrNull()
        assertNotNull( registrationEvent )
        assertEquals( AssignedMasterDevice( device, registration ), registrationEvent.assignedMasterDevice )
    }

    @Test
    fun updateDeviceRegistration_does_not_trigger_event_for_unchanged_registration()
    {
        val protocol = createSingleMasterDeviceProtocol()
        val device = protocol.masterDevices.first()
        val group = createParticipantGroup( protocol )
        val registration = device.createRegistration()
        group.updateDeviceRegistration( device, registration )

        group.consumeEvents()
        group.updateDeviceRegistration( device, registration )
        assertEquals( 0, group.consumeEvents().size )
    }

    @Test
    fun updateDeviceRegistration_fails_for_unknown_device()
    {
        val group = createParticipantGroup()

        val unknownDevice = StubMasterDeviceDescriptor()
        assertFailsWith<IllegalArgumentException>
        {
            group.updateDeviceRegistration( unknownDevice, null )
        }
        assertEquals( 0, group.consumeEvents().filterIsInstance<ParticipantGroup.Event.DeviceRegistrationChanged>().count() )
    }

    @Test
    fun studyDeploymentStopped_succeeds()
    {
        val group = createParticipantGroup()

        group.studyDeploymentStopped()

        assertTrue( group.isStudyDeploymentStopped )
        assertEquals( 1, group.consumeEvents().filterIsInstance<ParticipantGroup.Event.StudyDeploymentStopped>().count() )
    }

    @Test
    fun setData_succeeds()
    {
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
        protocol.addExpectedParticipantData( ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ) )

        val group = createParticipantGroup( protocol )

        group.setData( CarpInputDataTypes, CarpInputDataTypes.SEX, Sex.Male )
        assertEquals( Sex.Male, group.data[ CarpInputDataTypes.SEX ] )
    }

    @Test
    fun setData_fails_for_unexpected_data()
    {
        val group = createParticipantGroup()

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
        val group = createParticipantGroup( protocol )

        val wrongData = object : Data { }
        assertFailsWith<IllegalArgumentException>
        {
            group.setData( CarpInputDataTypes, CarpInputDataTypes.SEX, wrongData )
        }
    }

    private fun createParticipantGroup( protocol: StudyProtocol = createSingleMasterDeviceProtocol() ): ParticipantGroup
    {
        val deployment = StudyDeployment( protocol.getSnapshot() )
        return ParticipantGroup.fromNewDeployment( deployment )
    }
}
