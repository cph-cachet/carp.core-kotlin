package dk.cachet.carp.common


/**
 * Represents the address of an electronic mail sender or recipient.
 * TODO: Validate so only valid email addresses can be passed.
 */
data class EmailAddress( val address: String )