package dk.cachet.carp.common.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * Defines a type of data which can be processed by the platform (e.g., measured/collected/uploaded).
 * This is used by the infrastructure to determine whether the requested data can be collected on a device,
 * how to upload it, how to process it in a secondary data stream, or how triggers can act on it.
 */
@Serializable( with = DataTypeSerializer::class )
data class DataType(
    /**
     * Uniquely identifies the organization/person who determines how to interpret [name].
     * To prevent conflicts, a reverse domain namespace is suggested: e.g., "org.openmhealth" or "dk.cachet.carp".
     */
    val namespace: String,
    /**
     * Describes the data being collected (e.g., "acceleration", "stepcount", "audio"), but not the sensor (e.g., "accelerometer, "pedometer").
     */
    val name: String
)


object DataTypeSerializer : KSerializer<DataType>
{
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("dk.cachet.carp.protocols.domain.data.DataType", PrimitiveKind.STRING )


    override fun serialize( encoder: Encoder, value: DataType )
    {
        encoder.encodeString( "${value.namespace}.${value.name}" )
    }

    override fun deserialize( decoder: Decoder ): DataType
    {
        val dataType = decoder.decodeString()
        val segments = dataType.split( '.' )

        val namespace = segments.subList( 0, segments.size - 1 ).joinToString( "." )
        val name = segments.last()

        return DataType( namespace, name )
    }
}
