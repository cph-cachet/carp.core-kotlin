package dk.cachet.carp.common.users


/**
 * Domain service for account management which should only be used internally and not be exposed as an application service.
 */
interface AccountService
{
    /**
     * Create a new account for the person identified by [identity] to participate in a study.
     * The invitation and account details should be delivered, or made available, to the person managing the [identity].
     *
     * @throws IllegalArgumentException when an account with a matching [AccountIdentity] already exists.
     */
    suspend fun inviteNewAccount( identity: AccountIdentity ): Account

    /**
     * Deliver an invitation to participate in a study, or make it available, to the person managing [identity].
     *
     * @throws IllegalArgumentException when no account with a matching [identity] exists.
     */
    suspend fun inviteExistingAccount( identity: AccountIdentity )

    /**
     * Returns the [Account] which has the specified [identity], or null when no account is found.
     */
    suspend fun findAccount( identity: AccountIdentity ): Account?
}
