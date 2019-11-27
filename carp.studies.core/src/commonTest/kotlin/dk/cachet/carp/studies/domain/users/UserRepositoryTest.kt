package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
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
        val account1 = Account( username1, setOf(), id )
        val account2 = Account( username2, setOf(), id )
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
        val studyId = UUID.randomUUID()
        val participant = Participant( studyId )

        repo.addStudyParticipation( account.id, participant )
        val participants = repo.getParticipantsForStudy( studyId )

        assertEquals( participant, participants.single() )
    }

    @Test
    fun addStudyParticipation_for_unknown_account_fails()
    {
        val repo = createUserRepository()
        val studyId = UUID.randomUUID()
        val unknownId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repo.addStudyParticipation( unknownId, Participant( studyId ) )
        }
    }

    @Test
    fun addStudyParticipation_with_existing_participant_only_adds_once()
    {
        val ( repo, account ) = createRepoWithTestAccount()
        val studyId = UUID.randomUUID()
        val participant = Participant( studyId )

        repo.addStudyParticipation( account.id, participant )
        repo.addStudyParticipation( account.id, participant )
        val participants = repo.getParticipantsForStudy( studyId )

        assertEquals( participant, participants.single() )
    }

    @Test
    fun getParticipantsForStudy_returns_study_participants_only()
    {
        val ( repo, account ) = createRepoWithTestAccount()
        val studyId = UUID.randomUUID()
        val studyParticipants = listOf( Participant( studyId ), Participant( studyId ) )
        val otherParticipant = Participant( UUID.randomUUID() ) // Some other study.

        (studyParticipants + otherParticipant).forEach { repo.addStudyParticipation( account.id, it )  }
        val participants = repo.getParticipantsForStudy( studyId )

        assertEquals( 2, participants.intersect( studyParticipants ).count() )
    }
}
