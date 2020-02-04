package dk.cachet.carp.common.users

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies an account with associated identities and the studies it participates in.
 */
@Serializable
data class Account(
    /**
     * Identity associated with this account.
     */
    val identity: AccountIdentity,
    val id: UUID = UUID.randomUUID()
)
{
    companion object
    {
        /**
         * Create a new [Account] uniquely identified by the specified [emailAddress].
         */
        fun withEmailIdentity( emailAddress: String ): Account =
            Account( EmailAccountIdentity( emailAddress ) )

        /**
         * Create a new [Account] uniquely identified by the specified [emailAddress].
         */
        fun withEmailIdentity( emailAddress: EmailAddress ): Account =
            Account( EmailAccountIdentity( emailAddress ) )

        /**
         * Create a new [Account] uniquely identified by the specified [username].
         */
        fun withUsernameIdentity( username: String ): Account =
            Account( UsernameAccountIdentity( username ) )

        /**
         * Create a new [Account] uniquely identified by the specified [username].
         */
        fun withUsernameIdentity( username: Username ): Account =
            Account( UsernameAccountIdentity( username ) )
    }

    /**
     * Determines whether this account has the same [identity] as [otherAccount].
     */
    fun hasSameIdentity( otherAccount: Account ): Boolean =
        identity == otherAccount.identity
}
