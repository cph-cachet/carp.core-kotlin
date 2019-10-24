package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.*


/**
 * A [UserRepository] which holds accounts and participants in memory as long as the instance is held in memory.
 */
class InMemoryUserRepository : UserRepository
{
    private val accounts: MutableList<Account> = mutableListOf()


    override fun addAccount( account: Account )
    {
        require( accounts.none { it.id == account.id || it.emailAddress == account.emailAddress } )

        accounts.add( account )
    }

    override fun findAccountWithEmail( emailAddress: EmailAddress ): Account?
        = accounts.firstOrNull { it.emailAddress == emailAddress }

    override fun addStudyParticipation( accountId: UUID, participant: Participant )
    {
        val account = accounts.firstOrNull { it.id == accountId }
        require( account != null )

        // Remove account as it will be re-added.
        accounts.remove( account )

        // Clone removed account with added participation.
        val newAccount = Account(
            account.emailAddress,
            account.studyParticipations.plus( participant ),
            account.id )

        accounts.add( newAccount )
    }

    override fun getParticipantsForStudy( studyId: UUID ): List<Participant>
        = accounts.fold( mutableListOf() ) { participants, account ->
            val participations = account.studyParticipations.filter { it.studyId == studyId }
            participants.addAll( participations )
            participants
        }
}


/**
 * Tests whether the [InMemoryUserRepository] stub is implemented correctly.
 */
class InMemoryUserRepositoryTest : UserRepositoryTest
{
    override fun createUserRepository(): UserRepository = InMemoryUserRepository()
}