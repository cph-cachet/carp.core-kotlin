package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.EmailAddress


/**
 * Identifies an [Account].
 */
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
 * Identifies an [Account] by an [emailAddress] somebody has access to.
 */
data class EmailAccountIdentity( val emailAddress: EmailAddress ) : AccountIdentity
{
    constructor( emailAddress: String ) : this( EmailAddress( emailAddress ) )
}

/**
 * Identifies an [Account] by a unique [username].
 */
data class UsernameAccountIdentity( val username: Username ) : AccountIdentity
{
    constructor( username: String ) : this( Username( username ) )
}