package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.AccountService
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for implementations of [ParticipationService].
 */
abstract class ParticipationServiceTest
{
    /**
     * Create a participation service and account service it depends on to be used in the tests.
     */
    abstract fun createService(): Pair<ParticipationService, AccountService>


    @Test
    fun addParticipation_has_matching_studyDeploymentId() = runBlockingTest {
        val ( participationService, _ ) = createService()

        val accountIdentity = AccountIdentity.fromUsername( "test" )
        val studyDeploymentId = UUID.randomUUID()
        val participation = participationService.addParticipation( studyDeploymentId, accountIdentity )

        assertEquals( studyDeploymentId, participation.studyDeploymentId )
    }

    @Test
    fun addParticipation_creates_new_account_for_new_identity() = runBlockingTest {
        val ( participationService, accountService ) = createService()

        val studyDeploymentId = UUID.randomUUID()
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        participationService.addParticipation( studyDeploymentId, emailIdentity )

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

        // Adding the same identity to a deployment returns the same participation.
        val p1: Participation = participationService.addParticipation( studyDeploymentId, emailIdentity )
        val p2: Participation = participationService.addParticipation( studyDeploymentId, emailIdentity )
        assertTrue( p1.id == p2.id )
    }

    @Test
    fun getParticipantsForStudy_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val accountIdentity = AccountIdentity.fromUsername( "test" )
        val studyDeploymentId = UUID.randomUUID()
        val participation = service.addParticipation( studyDeploymentId, accountIdentity )

        val participations = service.getParticipationsForStudyDeployment( studyDeploymentId )

        assertEquals( participation, participations.single() )
    }
}
