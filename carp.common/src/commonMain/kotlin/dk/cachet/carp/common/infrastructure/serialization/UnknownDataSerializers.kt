package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.*
import kotlinx.serialization.json.Json


/**
 * A wrapper used to load extending types from [Data] serialized as JSON which are unknown at runtime.
 */
@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( DataSerializer::class )
data class CustomData internal constructor(
    override val className: String,
    override val jsonSource: String,
    val serializer: Json
) : Data, UnknownPolymorphicWrapper

/**
 * Custom serializer for [Data] which enables deserializing types that are unknown at runtime, yet extend from [Data].
 */
object DataSerializer : KSerializer<Data> by createUnknownPolymorphicSerializer(
    { className, json, serializer -> CustomData( className, json, serializer ) }
)
