package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.AccountRepository
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.UserRepository
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for implementations of [ParticipationService].
 */
abstract class ParticipationServiceTest
{
    /**
     * Create a user service and repositories it depends on to be used in the tests.
     */
    abstract fun createUserService(): Triple<ParticipationService, UserRepository, AccountRepository>


    @Test
    fun addParticipation_has_matching_studyDeploymentId() = runBlockingTest {
        val ( service, _, _ ) = createUserService()

        val accountIdentity = AccountIdentity.fromUsername( "test" )
        val studyDeploymentId = UUID.randomUUID()
        val participation = service.addParticipation( studyDeploymentId, accountIdentity )

        assertEquals( studyDeploymentId, participation.studyDeploymentId )
    }

    @Test
    fun addParticipation_creates_new_account_for_new_identity() = runBlockingTest {
        val ( service, _, accountRepo ) = createUserService()

        val studyDeploymentId = UUID.randomUUID()
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        service.addParticipation( studyDeploymentId, emailIdentity )

        // Verify whether account was added to the repository.
        val foundAccount = accountRepo.findAccountWithIdentity( emailIdentity )
        assertNotNull( foundAccount )
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipation_with_same_studyDeploymentId_and_identity() = runBlockingTest {
        val ( service, _, _ ) = createUserService()
        val studyDeploymentId = UUID.randomUUID()
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )

        // Adding the same identity to a deployment returns the same participation.
        val p1: Participation = service.addParticipation( studyDeploymentId, emailIdentity )
        val p2: Participation = service.addParticipation( studyDeploymentId, emailIdentity )
        assertTrue( p1.id == p2.id )
    }

    @Test
    fun getParticipantsForStudy_succeeds() = runBlockingTest {
        val ( service, _, _ ) = createUserService()
        val accountIdentity = AccountIdentity.fromUsername( "test" )
        val studyDeploymentId = UUID.randomUUID()
        val participation = service.addParticipation( studyDeploymentId, accountIdentity )

        val participations = service.getParticipationsForStudyDeployment( studyDeploymentId )

        assertEquals( participation, participations.single() )
    }
}
