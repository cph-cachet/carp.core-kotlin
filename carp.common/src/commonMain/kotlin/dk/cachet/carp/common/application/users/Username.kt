package dk.cachet.carp.common.application.users

import kotlinx.serialization.Serializable


/**
 * A unique name which identifies an account.
 */
@Serializable
data class Username( val name: String )
