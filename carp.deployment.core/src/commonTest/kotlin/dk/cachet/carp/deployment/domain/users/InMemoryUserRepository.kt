package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity


/**
 * A [UserRepository] which holds accounts and participations in memory as long as the instance is held in memory.
 */
class InMemoryUserRepository : UserRepository
{
    private val accounts: MutableList<Account> = mutableListOf()
    private val participations: MutableMap<UUID, MutableSet<Participation>> = mutableMapOf()


    override fun addAccount( account: Account )
    {
        require( accounts.none { it.id == account.id || it.hasSameIdentity( account ) } )

        accounts.add( account )
    }

    override fun findAccountWithId( accountId: UUID ): Account? =
        accounts.firstOrNull { it.id == accountId }

    override fun findAccountWithIdentity( identity: AccountIdentity ): Account? =
        accounts.firstOrNull { it.identity == identity }

    override fun addParticipation( accountId: UUID, participation: Participation )
    {
        val account = accounts.firstOrNull { it.id == accountId }
        require( account != null )

        val accountParticipations = participations.getOrPut( accountId ) { mutableSetOf() }
        accountParticipations.add( participation )
    }

    override fun getParticipations( accountId: UUID ): List<Participation> =
        participations[ accountId ]?.toList() ?: listOf()

    override fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation> =
        participations.flatMap { it.component2().filter { p -> p.studyDeploymentId == studyDeploymentId } }
}


/**
 * Tests whether the [InMemoryUserRepository] stub is implemented correctly.
 */
class InMemoryUserRepositoryTest : UserRepositoryTest
{
    override fun createUserRepository(): UserRepository = InMemoryUserRepository()
}
