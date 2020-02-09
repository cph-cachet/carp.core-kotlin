package dk.cachet.carp.common.users

import dk.cachet.carp.common.EmailAddress


class AccountServiceHost( private val repository: AccountRepository, private val notifyUserService: NotifyUserService ) : AccountService
{
    /**
     * Create an account which is identified by a unique [username].
     *
     * @throws IllegalArgumentException when an [Account] with the specified [username] already exists.
     */
    override suspend fun createAccount( username: Username ): Account
    {
        require( repository.findAccountWithIdentity( UsernameAccountIdentity( username ) ) == null )

        val account = Account.withUsernameIdentity( username )
        repository.addAccount( account )

        return account
    }

    /**
     * Create an account which is identified by an [emailAddress] someone has access to.
     * In case no [Account] is associated with the specified [emailAddress], send out a verification email.
     */
    override suspend fun createAccount( emailAddress: EmailAddress )
    {
        val existingAccount = repository.findAccountWithIdentity( EmailAccountIdentity( emailAddress ) )
        val isNewAccount = existingAccount == null

        if ( isNewAccount )
        {
            val newAccount = Account.withEmailIdentity( emailAddress )
            repository.addAccount( newAccount )
            notifyUserService.sendAccountVerificationEmail( newAccount.id, emailAddress )
        }
    }
}
