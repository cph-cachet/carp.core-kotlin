package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*


/**
 * Serializes a [Map] as an array of JSON objects which each have a "key" and "value"
 * instead of a single JSON object containing values for all keys in the map, which is the default.
 *
 * This is useful when using JSON serialization and the serialized keys in a map violate JSON recommendations.
 * E.g., the key may include discouraged symbols such as '.', or '$' for BSON.
 */
class MapAsArraySerializer<K, V>(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>
) : KSerializer<Map<K, V>>
{
    @Serializable
    data class KeyValue<K, V>( val key: K, val value: V )

    private val arraySerializer = ListSerializer( KeyValue.serializer( keySerializer, valueSerializer ) )
    override val descriptor: SerialDescriptor = arraySerializer.descriptor

    override fun deserialize( decoder: Decoder ): Map<K, V> = decoder
        .decodeSerializableValue( arraySerializer )
        .associate { it.key to it.value }

    override fun serialize( encoder: Encoder, value: Map<K, V> ) = encoder
        .encodeSerializableValue(
            arraySerializer,
            value.map { KeyValue( it.key, it.value ) }
        )
}
