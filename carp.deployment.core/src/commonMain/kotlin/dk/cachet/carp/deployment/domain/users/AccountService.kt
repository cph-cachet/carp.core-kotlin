package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity


/**
 * Domain service for account management which should only be used internally and not be exposed as an application service.
 */
interface AccountService
{
    /**
     * Create a new account identified by [identity] to participate in a study deployment with the given [participation] details.
     * An invitation and account details should be delivered, or made available, to the user managing the [identity].
     *
     * @throws IllegalArgumentException when an account with a matching [AccountIdentity] already exists.
     */
    suspend fun inviteNewAccount( identity: AccountIdentity, participation: Participation ): Account

    /**
     * Provide [participation] details, or make it available, to the user managing [identity].
     *
     * @throws IllegalArgumentException when no account with a matching [identity] exists.
     */
    suspend fun inviteExistingAccount( identity: AccountIdentity, participation: Participation )

    /**
     * Returns the [Account] which has the specified [identity], or null when no account is found.
     */
    suspend fun findAccount( identity: AccountIdentity ): Account?
}
