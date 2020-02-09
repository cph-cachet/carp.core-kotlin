package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.AccountRepository


/**
 * An [AccountRepository] which holds accounts in memory as long as the instance is held in memory.
 */
class InMemoryAccountRepository : AccountRepository
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
}
