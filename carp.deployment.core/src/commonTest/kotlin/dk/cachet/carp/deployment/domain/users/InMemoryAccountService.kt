package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity


/**
 * An [AccountService] which holds accounts in memory as long as the instance is held in memory.
 */
class InMemoryAccountService : AccountService
{
    private val accounts: MutableList<Account> = mutableListOf()


    override suspend fun inviteNewAccount( identity: AccountIdentity ): Account
    {
        require( accounts.none { it.identity == identity } )

        val account = Account( identity )
        accounts.add( account )

        return account
    }

    override suspend fun inviteExistingAccount( identity: AccountIdentity )
    {
        require( accounts.any { it.identity == identity } )
    }

    override suspend fun findAccount( identity: AccountIdentity ): Account? =
        accounts.firstOrNull { it.identity == identity }
}

/**
 * Tests whether the [InMemoryAccountService] stub is implemented correctly.
 */
class InMemoryAccountServiceTest : AccountServiceTest()
{
    override fun createService(): AccountService = InMemoryAccountService()
}
