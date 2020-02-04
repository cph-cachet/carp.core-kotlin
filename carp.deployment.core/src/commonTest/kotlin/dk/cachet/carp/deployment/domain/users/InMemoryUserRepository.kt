package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity


/**
 * A [UserRepository] which holds accounts and participants in memory as long as the instance is held in memory.
 */
class InMemoryUserRepository : UserRepository
{
    private val accounts: MutableList<Account> = mutableListOf()
    private val participations: MutableMap<UUID, MutableSet<Participant>> = mutableMapOf()


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

        val accountParticipations = participations.getOrPut( accountId ) { mutableSetOf() }
        accountParticipations.add( participant )
    }

    override fun getStudyParticipations( accountId: UUID ): List<Participant> =
        participations[ accountId ]?.toList() ?: listOf()

    override fun getParticipantsForStudy( studyId: UUID ): List<Participant> =
        participations.flatMap { it.component2().filter { p -> p.studyId == studyId } }
}


/**
 * Tests whether the [InMemoryUserRepository] stub is implemented correctly.
 */
class InMemoryUserRepositoryTest : UserRepositoryTest
{
    override fun createUserRepository(): UserRepository = InMemoryUserRepository()
}
