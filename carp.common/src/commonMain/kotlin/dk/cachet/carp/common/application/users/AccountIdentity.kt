package dk.cachet.carp.common.application.users

import dk.cachet.carp.common.application.EmailAddress
import kotlinx.serialization.*


/**
 * Identifies an account.
 */
@Polymorphic
interface AccountIdentity
{
    companion object Factory
    {
        /**
         * Create an [AccountIdentity] identified by an [emailAddress] somebody has access to.
         */
        fun fromEmailAddress( emailAddress: String ) = EmailAccountIdentity( emailAddress )

        /**
         * Create an [AccountIdentity] identified by a unique [username].
         */
        fun fromUsername( username: String ) = UsernameAccountIdentity( username )
    }
}

/**
 * Identifies an account by an [emailAddress] somebody has access to.
 */
@Serializable
data class EmailAccountIdentity( val emailAddress: EmailAddress ) : AccountIdentity
{
    constructor( emailAddress: String ) : this( EmailAddress( emailAddress ) )
}

/**
 * Identifies an account by a unique [username].
 */
@Serializable
data class UsernameAccountIdentity( val username: Username ) : AccountIdentity
{
    constructor( username: String ) : this( Username( username ) )
}
