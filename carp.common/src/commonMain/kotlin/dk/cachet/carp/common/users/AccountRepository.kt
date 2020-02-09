package dk.cachet.carp.common.users

import dk.cachet.carp.common.UUID


interface AccountRepository
{
    /**
     * Add a new [account] to the repository.
     *
     * @throws IllegalArgumentException when an [account] with the same id or a matching [AccountIdentity] already exists.
     */
    fun addAccount( account: Account )

    /**
     * Returns the [Account] which has the specified [accountId], or null when no account is found.
     */
    fun findAccountWithId( accountId: UUID ): Account?

    /**
     * Returns the [Account] which has the specified [identity], or null when no account is found.
     */
    fun findAccountWithIdentity( identity: AccountIdentity ): Account?
}
