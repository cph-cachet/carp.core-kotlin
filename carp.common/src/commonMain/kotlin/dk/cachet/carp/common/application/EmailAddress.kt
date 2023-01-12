package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createCarpStringPrimitiveSerializer
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Represents the address of an electronic mail sender or recipient.
 * TODO: Validate so only valid email addresses can be passed.
 */
@Serializable( EmailAddressSerializer::class )
@JsExport
data class EmailAddress( val address: String )
{
    override fun toString(): String = address
}


/**
 * A custom serializer for [EmailAddress].
 */
object EmailAddressSerializer : KSerializer<EmailAddress> by
    createCarpStringPrimitiveSerializer( { EmailAddress( it ) } )
