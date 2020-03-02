package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.createSingleMasterWithConnectedDeviceProtocol
import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for implementations of [DeploymentService].
 */
abstract class DeploymentServiceTest
{
    /**
     * Create a deployment service and account service it depends on to be used in the tests.
     */
    abstract fun createService(): Pair<DeploymentService, AccountService>


    @Test
    fun addParticipation_has_matching_studyDeploymentId() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val deviceRoleName = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRoleName )

        val accountIdentity = AccountIdentity.fromUsername( "test" )
        val invitation = StudyInvitation.empty()
        val participation = deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), accountIdentity, invitation )

        assertEquals( studyDeploymentId, participation.studyDeploymentId )
    }

    @Test
    fun addParticipation_creates_new_account_for_new_identity() = runBlockingTest {
        val ( deploymentService, accountService ) = createService()
        val deviceRoleName = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRoleName )

        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()
        deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )

        // Verify whether account was added.
        val foundAccount = accountService.findAccount( emailIdentity )
        assertNotNull( foundAccount )
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipation_with_same_studyDeploymentId_and_identity() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val deviceRoleName = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRoleName )

        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()

        // Adding the same identity to a deployment returns the same participation.
        val p1: Participation = deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )
        val p2: Participation = deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )
        assertTrue( p1.id == p2.id )
    }

    @Test
    fun addParticipation_fails_for_second_differing_request() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val deviceRoleName = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRoleName )
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()
        deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )

        val differentInvitation = StudyInvitation( "Different" )
        assertFailsWith<IllegalStateException>
        {
            deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, differentInvitation )
        }
    }

    @Test
    fun addParticipation_fails_for_unknown_studyDeploymentId() = runBlockingTest {
        val ( deploymentService, _ ) = createService()

        val unknownId = UUID.randomUUID()
        val identity = AccountIdentity.fromUsername( "test" )
        assertFailsWith<IllegalArgumentException>
        {
            deploymentService.addParticipation( unknownId, setOf( "Device" ), identity, StudyInvitation.empty() )
        }
    }

    @Test
    fun addParticipation_fails_for_unknown_deviceRoleNames() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Some device" )
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()

        assertFailsWith<IllegalArgumentException>
        {
            deploymentService.addParticipation( studyDeploymentId, setOf( "Wrong device" ), emailIdentity, invitation )
        }
    }

    @Test
    fun addParticipation_and_retrieving_invitation_succeeds() = runBlockingTest {
        val ( deploymentService, accountService ) = createService()
        val deviceRoleName = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRoleName )
        val identity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()

        val participation = deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), identity, invitation )
        val account = accountService.findAccount( identity )
        assertNotNull( account )
        val retrievedInvitations = deploymentService.getParticipationInvitations( account.id )
        assertEquals( ParticipationInvitation( participation, invitation, setOf( deviceRoleName ) ), retrievedInvitations.single() )
    }


    /**
     * Create a deployment to be used in tests in the given [deploymentService]
     * with a protocol containing a single master device with the specified [deviceRoleName].
     */
    private suspend fun addTestDeployment( deploymentService: DeploymentService, deviceRoleName: String ): UUID
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol( deviceRoleName )
        val snapshot = protocol.getSnapshot()
        val status = deploymentService.createStudyDeployment( snapshot )

        return status.studyDeploymentId
    }
}
