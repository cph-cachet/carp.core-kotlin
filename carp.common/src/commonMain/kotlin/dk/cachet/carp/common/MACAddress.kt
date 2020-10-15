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
 *
 * TODO: Allow more flexible [address] input and conversion to the common form. Make sure equality is ensured!
 */
@Serializable( with = MACAddressSerializer::class )
data class MACAddress( val address: String )
{
    init
    {
        require( MACAddressRegex.matches( address ) )
            { "Invalid MAC address string representation. Expected six groups of two upper case hexadecimal digits, separated by hyphens (-)." }
    }
}


/**
 * Regular expression to verify whether the string representation of a [MACAddress] matches the IEEE 802 standard notation.
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
