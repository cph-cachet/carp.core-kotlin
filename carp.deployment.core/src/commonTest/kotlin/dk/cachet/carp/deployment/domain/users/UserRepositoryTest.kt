package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.UsernameAccountIdentity
import kotlin.test.*


/**
 * Tests for implementations of [UserRepository].
 */
interface UserRepositoryTest
{
    fun createUserRepository(): UserRepository


    fun createRepoWithTestAccount(): Pair<UserRepository, Account>
    {
        val repo = createUserRepository()
        val testAccount = Account.withUsernameIdentity( "test" )

        repo.addAccount( testAccount )
        return Pair( repo, testAccount )
    }

    @Test
    fun cant_add_account_with_id_that_already_exists()
    {
        val repo = createUserRepository()
        val id = UUID.randomUUID()
        val username1 = UsernameAccountIdentity( "test" )
        val username2 = UsernameAccountIdentity( "test2" )
        val account1 = Account( username1, id )
        val account2 = Account( username2, id )
        repo.addAccount( account1 )

        assertFailsWith<IllegalArgumentException>
        {
            repo.addAccount( account2 )
        }
    }

    @Test
    fun cant_add_account_with_identity_that_already_exists()
    {
        val repo = createUserRepository()
        val username = "test"
        val account1 = Account.withUsernameIdentity( username )
        val account2 = Account.withUsernameIdentity( username )
        repo.addAccount( account1 )

        assertFailsWith<IllegalArgumentException>
        {
            repo.addAccount( account2 )
        }
    }

    @Test
    fun findAccountWithId_succeeds()
    {
        val ( repo, account ) = createRepoWithTestAccount()

        val foundAccount = repo.findAccountWithId( account.id )
        assertEquals( account, foundAccount )
    }

    @Test
    fun findAccountWithId_null_when_not_found()
    {
        val repo = createUserRepository()

        val foundAccount = repo.findAccountWithId( UUID.randomUUID() )
        assertNull( foundAccount )
    }

    @Test
    fun findAccountWithIdentity_succeeds()
    {
        val repo = createUserRepository()
        val username = "test"
        val account = Account.withUsernameIdentity( username )
        repo.addAccount( account )

        val foundAccount = repo.findAccountWithIdentity( AccountIdentity.fromUsername( username ) )
        assertEquals( account, foundAccount )
    }

    @Test
    fun findAccountWithIdentity_null_when_not_found()
    {
        val ( repo, _ ) = createRepoWithTestAccount()

        val foundAccount = repo.findAccountWithIdentity( AccountIdentity.fromEmailAddress( "non@existing.com" ) )
        assertNull( foundAccount )
    }

    @Test
    fun addStudyParticipation_and_retrieving_it_succeeds()
    {
        val ( repo, account ) = createRepoWithTestAccount()
        val studyDeploymentId = UUID.randomUUID()
        val participation = Participation( studyDeploymentId )

        repo.addParticipation( account.id, participation )
        val participations = repo.getParticipationsForStudyDeployment( studyDeploymentId )

        assertEquals( participation, participations.single() )
    }

    @Test
    fun addStudyParticipation_for_unknown_account_fails()
    {
        val repo = createUserRepository()
        val studyDeploymentId = UUID.randomUUID()
        val unknownId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repo.addParticipation( unknownId, Participation( studyDeploymentId ) )
        }
    }

    @Test
    fun addStudyParticipation_with_existing_participation_only_adds_once()
    {
        val ( repo, account ) = createRepoWithTestAccount()
        val studyDeploymentId = UUID.randomUUID()
        val participation = Participation( studyDeploymentId )

        repo.addParticipation( account.id, participation )
        repo.addParticipation( account.id, participation )
        val participations = repo.getParticipationsForStudyDeployment( studyDeploymentId )

        assertEquals( participation, participations.single() )
    }

    @Test
    fun getParticipationsForStudyDeployment_returns_matching_participations_only()
    {
        val ( repo, account ) = createRepoWithTestAccount()
        val studyDeploymentId = UUID.randomUUID()
        val participations = listOf( Participation( studyDeploymentId ), Participation( studyDeploymentId ) )
        val otherParticipations = Participation( UUID.randomUUID() ) // Some other study deployment.

        (participations + otherParticipations).forEach { repo.addParticipation( account.id, it ) }
        val retrievedParticipations = repo.getParticipationsForStudyDeployment( studyDeploymentId )

        assertEquals( 2, retrievedParticipations.intersect( participations ).count() )
    }
}
