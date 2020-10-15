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
 * @param address Six groups of two hexadecimal digits, separated by hyphens (-) or colons (:)
 *
 * TODO: It would probably be useful to allow even more flexible [address] entry (e.g., no/any separators, three groups with dot separator, ...).
 */
@Serializable( with = MACAddressSerializer::class )
class MACAddress( address: String )
{
    /**
     * The MAC address, represented according to the recommended IEEE 802 standard notation.
     */
    val address: String = address.toUpperCase().replace( ':', '-' )

    init
    {
        require( address.split( ':' ).size == GROUPS || address.split( '-' ).size == GROUPS )
            { "Invalid MAC address string representation: expected six groups of two hexadecimal digits, separated by hyphens (-) or colons (:)." }
        require( MACAddressRegex.matches( this.address ) )
            { "Invalid MAC address string representation: passed string contains non-hexadecimal digits." }
    }

    override fun equals( other: Any? ): Boolean = other is MACAddress && address == other.address
    override fun hashCode(): Int = address.hashCode()
    override fun toString(): String = address
}


private const val GROUPS: Int = 6 // Expected number of groups of hexadecimal digits.

/**
 * Regular expression to verify whether the string representation of a [MACAddress] matches the recommended IEEE 802 standard notation
 * using hyphens to separate six groups of two upper case hexadecimal digits. E.g.: '00-1B-44-11-3A-B7'.
 */
val MACAddressRegex = Regex( "([0-9A-F]{2}-){5}([0-9A-F]{2})" )


/**
 * A custom serializer for [MACAddress].
 */
object MACAddressSerializer : KSerializer<MACAddress>
{
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor( "dk.cachet.carp.common.MACAddress", PrimitiveKind.STRING )

    override fun serialize(encoder: Encoder, value: MACAddress ) = encoder.encodeString( value.address )
    override fun deserialize( decoder: Decoder): MACAddress = MACAddress( decoder.decodeString() )
}
