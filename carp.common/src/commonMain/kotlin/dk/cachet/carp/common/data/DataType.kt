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
     *
     * The name may not contain any periods. Periods are reserved for namespaces.
     */
    val name: String
)
{
    init
    {
        require( namespace.isNotEmpty() ) { "Namespace needs to be set." }
        require( !name.contains( '.' ) ) { "Name may not contain any periods. Periods are reserved for namespaces." }
    }

    companion object
    {
        /**
         * Initializes a [DataType] based on a [fullyQualifiedName], formatted as: "<namespace>.<name>".
         *
         * @throws IllegalArgumentException when no namespace is specified, i.e., [fullyQualifiedName] should contain at least one period.
         *   [name] will be set to the characters after the last period.
         */
        fun fromFullyQualifiedName( fullyQualifiedName: String ): DataType
        {
            val segments = fullyQualifiedName.split( '.' )
            require( segments.count() > 1 ) { "A namespace needs to be specified." }

            val namespace = segments.subList( 0, segments.size - 1 ).joinToString( "." )
            val name = segments.last()

            return DataType( namespace, name )
        }
    }
}


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
        return DataType.fromFullyQualifiedName( dataType )
    }
}
