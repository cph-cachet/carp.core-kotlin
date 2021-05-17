package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.users.AccountService


/**
 * An [AccountService] which holds accounts in memory as long as the instance is held in memory.
 */
class InMemoryAccountService : AccountService
{
    private val accounts: MutableList<Account> = mutableListOf()


    /**
     * Create a new account identified by [identity] to participate in a study deployment with the given [participation] details.
     * The [invitation] and account details should be delivered, or made available, to the user managing the [identity].
     *
     * @throws IllegalArgumentException when an account with a matching [AccountIdentity] already exists.
     */
    override suspend fun inviteNewAccount(
        identity: AccountIdentity,
        invitation: StudyInvitation,
        participation: Participation,
        devices: List<AnyDeviceDescriptor>
    ): Account
    {
        require( accounts.none { it.identity == identity } )

        val account = Account( identity )
        accounts.add( account )

        return account
    }

    /**
     * Send out a [participation] [invitation] for a study, or make it available, to the account with [accountId].
     *
     * @throws IllegalArgumentException when account with [accountId] does not exist.
     */
    override suspend fun inviteExistingAccount(
        accountId: UUID,
        invitation: StudyInvitation,
        participation: Participation,
        devices: List<AnyDeviceDescriptor>
    )
    {
        require( accounts.any { it.id == accountId } )
    }

    /**
     * Returns the [Account] which has the specified [identity], or null when no account is found.
     */
    override suspend fun findAccount( identity: AccountIdentity ): Account? =
        accounts.firstOrNull { it.identity == identity }
}
