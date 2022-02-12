package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation


/**
 * Domain service for account management which should only be used internally and not be exposed as an application service.
 */
interface AccountService
{
    /**
     * Create a new account identified by [identity] to participate in a study deployment with the given [participation] details and [devices] to use.
     * The [invitation] and account details should be delivered, or made available, to the user managing the [identity].
     *
     * @throws IllegalArgumentException when an account with a matching [AccountIdentity] already exists.
     */
    suspend fun inviteNewAccount(
        identity: AccountIdentity,
        invitation: StudyInvitation,
        participation: Participation,
        devices: List<AnyDeviceConfiguration>
    ): Account

    /**
     * Send out a [participation] [invitation] and [devices] to use for a study, or make it available, to the account with [accountId].
     *
     * @throws IllegalArgumentException when account with [accountId] does not exist.
     */
    suspend fun inviteExistingAccount(
        accountId: UUID,
        invitation: StudyInvitation,
        participation: Participation,
        devices: List<AnyDeviceConfiguration>
    )

    /**
     * Returns the [Account] which has the specified [identity], or null when no account is found.
     */
    suspend fun findAccount( identity: AccountIdentity ): Account?
}
