package dk.cachet.carp.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * Represents a IEEE 802 48-bit media access control address (MAC address):
 * a unique identifier assigned to a network interface controller (NIC) for use as a network address in communications within a network segment.
 *
 * This is equivalent to the EUI-48 identifier.
 *
 * @param address Six groups of two upper case hexadecimal digits, separated by hyphens (-).
 */
@Serializable( with = MACAddressSerializer::class )
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
            { "Invalid MAC address string representation: expected six groups of two upper case hexadecimal digits, separated by hyphens (-)." }
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
                { "Invalid MAC address string representation: expected six groups of two hexadecimal digits (upper or lower case), separated by hyphens (-) or colons (:)." }

            val recommendedFormatting = address.toUpperCase().replace( ':', '-' )
            return MACAddress( recommendedFormatting )
        }
    }
}


/**
 * Regular expression to match [MACAddress] using the recommended IEEE 802 standard notation:
 * using hyphens to separate six groups of two upper case hexadecimal digits. E.g.: '00-1B-44-11-3A-B7'.
 */
val MACAddressRegex = Regex( "([0-9A-F]{2}-){5}([0-9A-F]{2})" )


/**
 * A custom serializer for [MACAddress].
 */
object MACAddressSerializer : KSerializer<MACAddress>
{
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor( "dk.cachet.carp.common.MACAddress", PrimitiveKind.STRING )

    override fun serialize( encoder: Encoder, value: MACAddress ) = encoder.encodeString( value.address )
    override fun deserialize( decoder: Decoder ): MACAddress = MACAddress( decoder.decodeString() )
}
