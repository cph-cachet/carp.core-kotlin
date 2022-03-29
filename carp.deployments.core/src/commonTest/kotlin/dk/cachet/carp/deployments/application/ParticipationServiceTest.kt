package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.deployments.application.users.AssignedPrimaryDevice
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.createParticipantInvitation
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.protocols.application.users.ExpectedParticipantData
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import kotlinx.coroutines.test.runTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [ParticipationService].
 */
interface ParticipationServiceTest
{
    companion object
    {
        private const val deviceRoleName: String = "Primary"
    }


    data class DependentServices(
        val participationService: ParticipationService,
        val deploymentService: DeploymentService,
        val accountService: AccountService,
        val eventBus: EventBus
    )

    /**
     * Create a deployment service and account service it depends on to be used in the tests.
     */
    fun createService(): DependentServices


    @Test
    fun getActiveParticipationInvitations_succeeds() = runTest {
        val (participationService, deploymentService, accountService) = createService()
        val protocol = createSinglePrimaryDeviceProtocol()
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
        val expectedAssignedDevice = AssignedPrimaryDevice( protocol.primaryDevices.single() )
        assertEquals( setOf( expectedAssignedDevice ), retrievedInvitation.assignedDevices )
    }

    @Test
    fun getParticipantData_initially_returns_null_for_all_expected_data() = runTest {
        val (participationService, deploymentService, _) = createService()

        // Create protocol with expected 'sex' participant data.
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        val defaultExpectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        )
        protocol.addExpectedParticipantData( defaultExpectedData )
        val customExpectedData = ExpectedParticipantData(
            ParticipantAttribute.CustomParticipantAttribute( Text( "Custom" ) )
        )
        protocol.addExpectedParticipantData( customExpectedData )
        val invitation = createParticipantInvitation( protocol )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        val participantData = participationService.getParticipantData( studyDeploymentId )
        assertEquals( studyDeploymentId, participantData.studyDeploymentId )
        assertEquals( setOf( CarpInputDataTypes.SEX, customExpectedData.inputDataType ), participantData.data.keys )
        assertTrue( participantData.data.values.all { it == null } )
    }

    @Test
    fun getParticipantData_fails_for_unknown_deploymentId() = runTest {
        val (participationService, _, _) = createService()

        assertFailsWith<IllegalArgumentException> { participationService.getParticipantData( unknownId ) }
    }

    @Test
    fun getParticipantDataList_succeeds() = runTest {
        val (participationService, deploymentService, _) = createService()
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
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
    fun getParticipantDataList_fails_for_unknown_deploymentId() = runTest {
        val (participationService, _, _) = createService()

        val deploymentIds = setOf( unknownId )
        assertFailsWith<IllegalArgumentException> { participationService.getParticipantDataList( deploymentIds ) }
    }

    @Test
    fun setParticipantData_succeeds() = runTest {
        val (participationService, deploymentService, _) = createService()

        // Create protocol with expected 'sex' participant data.
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        )
        protocol.addExpectedParticipantData( expectedData )
        val invitation = createParticipantInvitation( protocol )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        val toSet = mapOf( CarpInputDataTypes.SEX to Sex.Male )
        val afterSet = participationService.setParticipantData( studyDeploymentId, toSet )
        assertEquals( Sex.Male, afterSet.data[ CarpInputDataTypes.SEX ] )

        val retrievedData = participationService.getParticipantData( studyDeploymentId )
        assertEquals( afterSet, retrievedData )
    }

    @Test
    fun setParticipantData_fails_for_unknown_deploymentId() = runTest {
        val (participationService, _, _) = createService()

        val toSet = mapOf( CarpInputDataTypes.SEX to Sex.Male )
        assertFailsWith<IllegalArgumentException>
        {
            participationService.setParticipantData( unknownId, toSet )
        }
    }

    @Test
    fun setParticipantData_fails_for_unexpected_input_for_protocol() = runTest {
        val (participationService, deploymentService, _) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService )

        val toSet = mapOf( CarpInputDataTypes.SEX to Sex.Male )
        assertFailsWith<IllegalArgumentException>
        {
            participationService.setParticipantData( studyDeploymentId, toSet )
        }
    }

    @Test
    fun setParticipantData_fails_for_invalid_data() = runTest {
        val (participationService, deploymentService, _) = createService()

        // Create protocol with expected 'sex' participant data.
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        )
        protocol.addExpectedParticipantData( expectedData )
        val invitation = createParticipantInvitation( protocol )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        val toSet = mapOf( CarpInputDataTypes.SEX to StubDataPoint() )
        assertFailsWith<IllegalArgumentException>
        {
            participationService.setParticipantData( studyDeploymentId, toSet )
        }
    }

    /**
     * Add a test deployment to [deploymentService] for a protocol with a single primary device with [deviceRoleName].
     */
    private suspend fun addTestDeployment( deploymentService: DeploymentService ): UUID
    {
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        val invitation = createParticipantInvitation( protocol )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        return studyDeploymentId
    }
}
