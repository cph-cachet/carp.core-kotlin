package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.EmailAddress
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

        val id = UUID( "ce1fd1eb-aa35-4922-8dc4-c107d83357e1" )
        val testAccount = Account( EmailAddress( "test@test.com" ), setOf(), id )

        repo.addAccount( testAccount )
        return Pair( repo, testAccount )
    }

    @Test
    fun cant_add_account_with_id_that_already_exists()
    {
        val repo = createUserRepository()
        val id = UUID.randomUUID()
        val account1 = Account( EmailAddress( "test@test.com" ), setOf(), id )
        val account2 = Account( EmailAddress( "test2@test.com" ), setOf(), id )
        repo.addAccount( account1 )

        assertFailsWith<IllegalArgumentException>
        {
            repo.addAccount( account2 )
        }
    }

    @Test
    fun cant_add_account_with_email_that_already_exists()
    {
        val repo = createUserRepository()
        val email = EmailAddress( "test@test.com" )
        val account1 = Account( email, setOf() )
        val account2 = Account( email, setOf() )
        repo.addAccount( account1 )

        assertFailsWith<IllegalArgumentException>
        {
            repo.addAccount( account2 )
        }
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