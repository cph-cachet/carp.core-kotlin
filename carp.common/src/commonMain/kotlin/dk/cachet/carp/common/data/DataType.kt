package dk.cachet.carp.common.data

import dk.cachet.carp.common.FullyQualifiedName
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
     * Describes the data being collected (e.g., "acceleration", "stepcount", "audio"), but not the sensor (e.g., "accelerometer, "pedometer").
     */
    val name: FullyQualifiedName
)
{
    companion object
    {
        /**
         * Initializes a [DataType] based on a [fullyQualifiedName], formatted as: "<namespace>.<name>".
         *
         * @throws IllegalArgumentException when [fullyQualifiedName] is an invalid [FullyQualifiedName].
         */
        fun fromFullyQualifiedName( fullyQualifiedName: String ): DataType =
            DataType( FullyQualifiedName.fromString( fullyQualifiedName ) )
    }
}


object DataTypeSerializer : KSerializer<DataType>
{
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor( "dk.cachet.carp.common.data.DataType", PrimitiveKind.STRING )


    override fun serialize( encoder: Encoder, value: DataType ) =
        encoder.encodeSerializableValue( FullyQualifiedName.serializer(), value.name )

    override fun deserialize( decoder: Decoder ): DataType
    {
        val name: FullyQualifiedName = decoder.decodeSerializableValue( FullyQualifiedName.serializer() )
        return DataType( name )
    }
}
