package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.*
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies an account with associated identities and the studies it participates in.
 */
@Serializable
data class Account(
    /**
     * Identities associated with this account.
     */
    val identities: List<AccountIdentity> = listOf(),
    /**
     * The set of studies this account participates in as a participant.
     */
    val studyParticipations: Set<Participant> = setOf(),
    val id: UUID = UUID.randomUUID() )
{
    companion object
    {
        /**
         * Create a new [Account] uniquely identified by the specified [emailAddress].
         */
        fun withEmailIdentity( emailAddress: String ): Account
            = Account( listOf( EmailAccountIdentity( emailAddress ) ) )

        /**
         * Create a new [Account] uniquely identified by the specified [emailAddress].
         */
        fun withEmailIdentity( emailAddress: EmailAddress ): Account
            = Account( listOf( EmailAccountIdentity( emailAddress ) ) )

        /**
         * Create a new [Account] uniquely identified by the specified [username].
         */
        fun withUsernameIdentity( username: String ): Account
            = Account( listOf( UsernameAccountIdentity( username ) ) )

        /**
         * Create a new [Account] uniquely identified by the specified [username].
         */
        fun withUsernameIdentity( username: Username ): Account
            = Account( listOf( UsernameAccountIdentity( username ) ) )
    }

    /**
     * Determines whether this account has associated [identities] matching [otherAccount].
     */
    fun hasMatchingIdentity( otherAccount: Account ): Boolean
        = identities.intersect( otherAccount.identities ).any()
}