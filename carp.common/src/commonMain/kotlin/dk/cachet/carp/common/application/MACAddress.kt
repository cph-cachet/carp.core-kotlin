package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createCarpStringPrimitiveSerializer
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Represents an IEEE 802 48-bit media access control address (MAC address):
 * a unique identifier assigned to a network interface controller (NIC) for use as a network address in communications within a network segment.
 *
 * This is equivalent to the EUI-48 identifier.
 *
 * @param address Six groups of two upper case hexadecimal digits, separated by hyphens (-).
 */
@Serializable( with = MACAddressSerializer::class )
@JsExport
data class MACAddress(
    /**
     * The MAC address, represented according to the recommended IEEE 802 standard notation.
     * Six groups of two upper case hexadecimal digits, separate by hyphens (-).
     */
    val address: String
)
{
    init
    {
        require( MACAddressRegex.matches( this.address ) )
        {
            "Invalid MAC address string representation: " +
            "expected six groups of two upper case hexadecimal digits, separated by hyphens (-)."
        }
    }


    companion object
    {
        private const val GROUPS: Int = 6 // Expected number of groups of two hexadecimal digits.

        /**
         * Parse common MAC [address] representations to initialize a [MACAddress].
         * Six groups of two hexadecimal digits (upper or lower case), separated by hyphens (-) or colons (:).
         *
         * TODO: It might be useful to allow even more flexible [address] entry (e.g., no/any separators, three groups with dot separator, ...).
         */
        fun parse( address: String ): MACAddress
        {
            require( address.split( ':' ).size == GROUPS || address.split( '-' ).size == GROUPS )
            {
                "Invalid MAC address string representation: " +
                "expected six groups of two hexadecimal digits (upper or lower case), " +
                "separated by hyphens (-) or colons (:)."
            }

            val recommendedFormatting = address.uppercase().replace( ':', '-' )
            return MACAddress( recommendedFormatting )
        }
    }


    override fun toString(): String = address
}


/**
 * Regular expression to match [MACAddress] using the recommended IEEE 802 standard notation:
 * using hyphens to separate six groups of two upper case hexadecimal digits. E.g.: '00-1B-44-11-3A-B7'.
 */
val MACAddressRegex = Regex( "([0-9A-F]{2}-){5}([0-9A-F]{2})" )


/**
 * A custom serializer for [MACAddress].
 */
object MACAddressSerializer : KSerializer<MACAddress> by createCarpStringPrimitiveSerializer( { MACAddress( it ) } )
