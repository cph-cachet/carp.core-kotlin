package dk.cachet.carp.common.users


interface AccountService
{
    /**
     * Create an account which is identified by [identity].
     * Account details should be sent to the person holding the identity, or made retrievable for the person managing the specified [identity].
     *
     * @throws IllegalArgumentException when an account with a matching [AccountIdentity] already exists.
     */
    suspend fun createAccount( identity: AccountIdentity ): Account

    /**
     * Returns the [Account] which has the specified [identity], or null when no account is found.
     */
    suspend fun findAccount( identity: AccountIdentity ): Account?
}
