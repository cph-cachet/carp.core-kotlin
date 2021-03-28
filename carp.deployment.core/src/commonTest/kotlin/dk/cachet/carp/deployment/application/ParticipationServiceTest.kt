package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.data.input.element.Text
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.domain.users.ActiveParticipationInvitation
import dk.cachet.carp.deployment.domain.users.AssignedMasterDevice
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterDeviceProtocol
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [ParticipationService].
 */
abstract class ParticipationServiceTest
{
    /**
     * Create a deployment service and account service it depends on to be used in the tests.
     */
    abstract fun createService(
        participantDataInputTypes: InputDataTypeList = CarpInputDataTypes
    ): Triple<ParticipationService, DeploymentService, AccountService>


    @Test
    fun addParticipation_after_stop_not_allowed() = runSuspendTest {
        val (participationService, deploymentService, _) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService )
        deploymentService.stop( studyDeploymentId )

        val accountId = AccountIdentity.fromUsername( "Test" )
        val invitation = StudyInvitation.empty()
        assertFailsWith<IllegalStateException>
            { participationService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), accountId, invitation ) }
    }

    @Test
    fun addParticipation_has_matching_studyDeploymentId() = runSuspendTest {
        val (participationService, deploymentService, _) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService )

        val accountIdentity = AccountIdentity.fromUsername( "test" )
        val invitation = StudyInvitation.empty()
        val participation = participationService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), accountIdentity, invitation )

        assertEquals( studyDeploymentId, participation.studyDeploymentId )
    }

    @Test
    fun addParticipation_creates_new_account_for_new_identity() = runSuspendTest {
        val (participationService, deploymentService, accountService) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService )

        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()
        participationService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )

        // Verify whether account was added.
        val foundAccount = accountService.findAccount( emailIdentity )
        assertNotNull( foundAccount )
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipation_with_same_studyDeploymentId_and_identity() = runSuspendTest {
        val (participationService, deploymentService, _) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService )

        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()

        // Adding the same identity to a deployment returns the same participation.
        val p1: Participation = participationService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )
        val p2: Participation = participationService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )
        assertTrue( p1.id == p2.id )
    }

    @Test
    fun addParticipation_fails_for_second_differing_request() = runSuspendTest {
        val (participationService, deploymentService, _) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService )
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()
        participationService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )

        val differentInvitation = StudyInvitation( "Different", "New description" )
        assertFailsWith<IllegalStateException>
        {
            participationService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, differentInvitation )
        }
    }

    @Test
    fun addParticipation_fails_for_unknown_studyDeploymentId() = runSuspendTest {
        val (participationService, _, _) = createService()

        val identity = AccountIdentity.fromUsername( "test" )
        assertFailsWith<IllegalArgumentException>
        {
            participationService.addParticipation( unknownId, setOf( "Device" ), identity, StudyInvitation.empty() )
        }
    }

    @Test
    fun addParticipation_fails_for_unknown_deviceRoleNames() = runSuspendTest {
        val (participationService, deploymentService, _) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService )
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()

        assertFailsWith<IllegalArgumentException>
        {
            participationService.addParticipation( studyDeploymentId, setOf( "Wrong device" ), emailIdentity, invitation )
        }
    }

    @Test
    fun addParticipation_and_retrieving_invitation_succeeds() = runSuspendTest {
        val (participationService, deploymentService, accountService) = createService()
        val protocol = createSingleMasterDeviceProtocol()
        val studyDeploymentId = deploymentService.createStudyDeployment( protocol.getSnapshot() ).studyDeploymentId
        val identity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation( "Test study", "description", "Custom data" )

        val participation = participationService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), identity, invitation )
        val account = accountService.findAccount( identity )
        assertNotNull( account )
        val retrievedInvitations = participationService.getActiveParticipationInvitations( account.id )
        val masterDevice = protocol.masterDevices.single()
        val expectedAssignedDevice = AssignedMasterDevice( masterDevice, null )
        assertEquals( ActiveParticipationInvitation( participation, invitation, setOf( expectedAssignedDevice ) ), retrievedInvitations.single() )
    }

    @Test
    fun getParticipantData_initially_returns_null_for_all_expected_data() = runSuspendTest {
        val (participationService, deploymentService, _) =
            createService( CarpInputDataTypes )

        // Create protocol with expected 'sex' participant data.
        val protocol = createSingleMasterDeviceProtocol( deviceRoleName )
        protocol.addExpectedParticipantData( ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ) )
        val customAttribute = ParticipantAttribute.CustomParticipantAttribute( Text( "Custom" ) )
        protocol.addExpectedParticipantData( customAttribute )
        val snapshot = protocol.getSnapshot()
        val status = deploymentService.createStudyDeployment( snapshot )

        val participantData = participationService.getParticipantData( status.studyDeploymentId )
        assertEquals( status.studyDeploymentId, participantData.studyDeploymentId )
        assertEquals( setOf( CarpInputDataTypes.SEX, customAttribute.inputType ), participantData.data.keys )
        assertTrue( participantData.data.values.all { it == null } )
    }

    @Test
    fun getParticipantData_fails_for_unknown_deploymentId() = runSuspendTest {
        val (participationService, _, _) = createService( CarpInputDataTypes )

        assertFailsWith<IllegalArgumentException> { participationService.getParticipantData( unknownId ) }
    }

    @Test
    fun getParticipantDataList_succeeds() = runSuspendTest {
        val (participationService, deploymentService, _) = createService()
        val protocol = createSingleMasterDeviceProtocol( deviceRoleName )
        val snapshot = protocol.getSnapshot()
        val deployment1 = deploymentService.createStudyDeployment( snapshot )
        val deployment2 = deploymentService.createStudyDeployment( snapshot )

        val deploymentIds = setOf( deployment1.studyDeploymentId, deployment2.studyDeploymentId )
        val participantDataList = participationService.getParticipantDataList( deploymentIds )
        assertEquals( 2, participantDataList.size )
    }

    @Test
    fun getParticipantDataList_fails_for_unknown_deploymentId() = runSuspendTest {
        val (participationService, _, _) = createService()

        val deploymentIds = setOf( unknownId )
        assertFailsWith<IllegalArgumentException> { participationService.getParticipantDataList( deploymentIds ) }
    }

    @Test
    fun setParticipantData_succeeds() = runSuspendTest {
        val (participationService, deploymentService, _) =
            createService( CarpInputDataTypes )

        // Create protocol with expected 'sex' participant data.
        val protocol = createSingleMasterDeviceProtocol( deviceRoleName )
        protocol.addExpectedParticipantData( ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ) )
        val snapshot = protocol.getSnapshot()
        val status = deploymentService.createStudyDeployment( snapshot )

        val afterSet = participationService.setParticipantData( status.studyDeploymentId, CarpInputDataTypes.SEX, Sex.Male )
        assertEquals( Sex.Male, afterSet.data[ CarpInputDataTypes.SEX ] )

        val retrievedData = participationService.getParticipantData( status.studyDeploymentId )
        assertEquals( afterSet, retrievedData )
    }

    @Test
    fun setParticipantData_fails_for_unknown_deploymentId() = runSuspendTest {
        val (participationService, _, _) = createService( CarpInputDataTypes )

        assertFailsWith<IllegalArgumentException>
        {
            participationService.setParticipantData( unknownId, CarpInputDataTypes.SEX, Sex.Male )
        }
    }

    @Test
    fun setParticipantData_fails_for_unexpected_input_for_protocol() = runSuspendTest {
        val (participationService, deploymentService, _) =
            createService( CarpInputDataTypes )
        val studyDeploymentId = addTestDeployment( deploymentService )

        assertFailsWith<IllegalArgumentException>
        {
            participationService.setParticipantData( studyDeploymentId, CarpInputDataTypes.SEX, Sex.Male )
        }
    }

    @Test
    fun setParticipantData_fails_for_invalid_data() = runSuspendTest {
        val (participationService, deploymentService, _) =
            createService( CarpInputDataTypes )

        // Create protocol with expected 'sex' participant data.
        val protocol = createSingleMasterDeviceProtocol( deviceRoleName )
        protocol.addExpectedParticipantData( ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ) )
        val snapshot = protocol.getSnapshot()
        val status = deploymentService.createStudyDeployment( snapshot )

        assertFailsWith<IllegalArgumentException>
        {
            val wrongData = object : Data { }
            participationService.setParticipantData( status.studyDeploymentId, CarpInputDataTypes.SEX, wrongData )
        }
    }


    private val deviceRoleName: String = "Master"

    /**
     * Add a test deployment to [deploymentService] for a protocol with a single master device with [deviceRoleName].
     */
    private suspend fun addTestDeployment( deploymentService: DeploymentService ): UUID
    {
        val protocol = createSingleMasterDeviceProtocol( deviceRoleName )
        val snapshot = protocol.getSnapshot()
        val status = deploymentService.createStudyDeployment( snapshot )

        return status.studyDeploymentId
    }
}
