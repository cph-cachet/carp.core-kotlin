package dk.cachet.carp.studies.domain.users

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
    /**
     * The set of studies this account participates in as a participant.
     */
    val studyParticipations: Set<Participant> = setOf(),
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
