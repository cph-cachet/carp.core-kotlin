package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.domain.users.Participation
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
        val ( participationService, _ ) = createService()

        val studyDeploymentId = UUID.randomUUID()
        val accountIdentity = AccountIdentity.fromUsername( "test" )
        val invitation = StudyInvitation.empty()
        val participation = participationService.addParticipation( studyDeploymentId, accountIdentity, invitation )

        assertEquals( studyDeploymentId, participation.studyDeploymentId )
    }

    @Test
    fun addParticipation_creates_new_account_for_new_identity() = runBlockingTest {
        val ( participationService, accountService ) = createService()

        val studyDeploymentId = UUID.randomUUID()
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()
        participationService.addParticipation( studyDeploymentId, emailIdentity, invitation )

        // Verify whether account was added.
        val foundAccount = accountService.findAccount( emailIdentity )
        assertNotNull( foundAccount )
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipation_with_same_studyDeploymentId_and_identity() = runBlockingTest {
        val ( participationService, _ ) = createService()
        val studyDeploymentId = UUID.randomUUID()
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()

        // Adding the same identity to a deployment returns the same participation.
        val p1: Participation = participationService.addParticipation( studyDeploymentId, emailIdentity, invitation )
        val p2: Participation = participationService.addParticipation( studyDeploymentId, emailIdentity, invitation )
        assertTrue( p1.id == p2.id )
    }

    @Test
    fun getParticipationssForStudyDeployment_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val studyDeploymentId = UUID.randomUUID()
        val accountIdentity = AccountIdentity.fromUsername( "test" )
        val invitation = StudyInvitation.empty()
        val participation = service.addParticipation( studyDeploymentId, accountIdentity, invitation )

        val participations = service.getParticipationsForStudyDeployment( studyDeploymentId )

        assertEquals( participation, participations.single() )
    }
}
