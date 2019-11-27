package dk.cachet.carp.common

import kotlinx.serialization.Serializable


/**
 * Represents the address of an electronic mail sender or recipient.
 * TODO: Validate so only valid email addresses can be passed.
 */
@Serializable
data class EmailAddress( val address: String )
