package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.deployments.application.users.AssignedMasterDevice
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.createParticipantInvitation
import dk.cachet.carp.deployments.domain.users.AccountService
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
    fun getActiveParticipationInvitations_succeeds() = runSuspendTest {
        val (participationService, deploymentService, accountService) = createService()
        val protocol = createSingleMasterDeviceProtocol()
        val identity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation( "Test study", "description", "Custom data" )
        val participantInvitation = ParticipantInvitation(
            participantId = UUID.randomUUID(),
            setOf( deviceRoleName ),
            identity,
            invitation
        )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( participantInvitation ) )

        val account = accountService.findAccount( identity )
        assertNotNull( account )
        val retrievedInvitation = participationService.getActiveParticipationInvitations( account.id ).singleOrNull()
        assertNotNull( retrievedInvitation )
        assertEquals( studyDeploymentId, retrievedInvitation.participation.studyDeploymentId )
        assertEquals( invitation, retrievedInvitation.invitation )
        val expectedAssignedDevice = AssignedMasterDevice( protocol.masterDevices.single() )
        assertEquals( setOf( expectedAssignedDevice ), retrievedInvitation.assignedDevices )
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
        val invitation = createParticipantInvitation( protocol )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        val participantData = participationService.getParticipantData( studyDeploymentId )
        assertEquals( studyDeploymentId, participantData.studyDeploymentId )
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
        val protocolSnapshot = protocol.getSnapshot()
        val invitation1 = createParticipantInvitation( protocol )
        val deploymentId1 = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId1, protocolSnapshot, listOf( invitation1 ) )
        val invitation2 = createParticipantInvitation( protocol )
        val deploymentId2 = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId2, protocolSnapshot, listOf( invitation2 ) )

        val deploymentIds = setOf( deploymentId1, deploymentId2 )
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
        val invitation = createParticipantInvitation( protocol )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        val afterSet = participationService.setParticipantData( studyDeploymentId, CarpInputDataTypes.SEX, Sex.Male )
        assertEquals( Sex.Male, afterSet.data[ CarpInputDataTypes.SEX ] )

        val retrievedData = participationService.getParticipantData( studyDeploymentId )
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
        val invitation = createParticipantInvitation( protocol )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        assertFailsWith<IllegalArgumentException>
        {
            val wrongData = object : Data { }
            participationService.setParticipantData( studyDeploymentId, CarpInputDataTypes.SEX, wrongData )
        }
    }


    private val deviceRoleName: String = "Master"

    /**
     * Add a test deployment to [deploymentService] for a protocol with a single master device with [deviceRoleName].
     */
    private suspend fun addTestDeployment( deploymentService: DeploymentService ): UUID
    {
        val protocol = createSingleMasterDeviceProtocol( deviceRoleName )
        val invitation = createParticipantInvitation( protocol )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        return studyDeploymentId
    }
}
