package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


/**
 * A wrapper used to load extending types from [SamplingConfiguration] serialized as JSON which are unknown at runtime.
 */
@Serializable( SamplingConfigurationSerializer::class )
data class CustomSamplingConfiguration(
    override val className: String,
    override val jsonSource: String,
    val serializer: Json
) : SamplingConfiguration, UnknownPolymorphicWrapper

/**
 * Custom serializer for a [SamplingConfiguration] which enables deserializing types that are unknown at runtime, yet extend from [SamplingConfiguration].
 */
object SamplingConfigurationSerializer : KSerializer<SamplingConfiguration>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomSamplingConfiguration( className, json, serializer ) } )
