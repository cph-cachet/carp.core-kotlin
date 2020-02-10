package dk.cachet.carp.common.users


/**
 * An [AccountService] which holds accounts in memory as long as the instance is held in memory.
 */
class InMemoryAccountService : AccountService
{
    private val accounts: MutableList<Account> = mutableListOf()


    override suspend fun createAccount( identity: AccountIdentity ): Account
    {
        require( accounts.none { it.identity == identity } )

        val account = Account( identity )
        accounts.add( account )

        return account
    }

    override suspend fun findAccount( identity: AccountIdentity ): Account? =
        accounts.firstOrNull { it.identity == identity }
}
