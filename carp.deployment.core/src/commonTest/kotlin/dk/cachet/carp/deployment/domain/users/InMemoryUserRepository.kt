package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID


/**
 * A [UserRepository] which holds accounts and participants in memory as long as the instance is held in memory.
 */
class InMemoryUserRepository : UserRepository
{
    private val accounts: MutableList<Account> = mutableListOf()


    override fun addAccount( account: Account )
    {
        require( accounts.none { it.id == account.id || it.hasSameIdentity( account ) } )

        accounts.add( account )
    }

    override fun findAccountWithId( accountId: UUID ): Account? =
        accounts.firstOrNull { it.id == accountId }

    override fun findAccountWithIdentity( identity: AccountIdentity ): Account? =
        accounts.firstOrNull { it.identity == identity }

    override fun addStudyParticipation( accountId: UUID, participant: Participant )
    {
        val account = accounts.firstOrNull { it.id == accountId }
        require( account != null )

        // Add participant to cloned account.
        val updatedParticipations = account.studyParticipations.plus( participant )
        val newAccount = account.copy( studyParticipations = updatedParticipations )

        // Remove and re-add account.
        accounts.remove( account )
        accounts.add( newAccount )
    }

    override fun getParticipantsForStudy( studyId: UUID ): List<Participant> =
        accounts.fold( mutableListOf() ) { participants, account ->
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
