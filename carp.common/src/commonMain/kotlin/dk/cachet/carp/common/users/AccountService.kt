package dk.cachet.carp.common.users

import dk.cachet.carp.common.EmailAddress


interface AccountService
{
    /**
     * Create an account which is identified by a unique [username].
     *
     * @throws IllegalArgumentException when an [Account] with the specified [username] already exists.
     */
    suspend fun createAccount( username: Username ): Account

    /**
     * Create an account which is identified by an [emailAddress] someone has access to.
     * In case no [Account] is associated with the specified [emailAddress], send out a confirmation email.
     */
    suspend fun createAccount( emailAddress: EmailAddress )
}
