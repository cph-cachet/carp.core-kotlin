package dk.cachet.carp.common.test.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import kotlinx.serialization.*
import kotlinx.serialization.json.*


/**
 * A non-typed version of [LoggedRequest], only mimicking the JSON structure.
 */
@ExperimentalSerializationApi
@Serializable
@JsonClassDiscriminator( "outcome" )
sealed class LoggedJsonRequest
{
    abstract val request: JsonObject
    abstract val precedingEvents: JsonArray
    abstract val publishedEvents: JsonArray

    @Serializable
    @SerialName( "Succeeded" )
    class Succeeded(
        override val request: JsonObject,
        override val precedingEvents: JsonArray,
        override val publishedEvents: JsonArray,
        val response: JsonElement
    ) : LoggedJsonRequest()

    @Serializable
    @SerialName( "Failed" )
    class Failed(
        override val request: JsonObject,
        override val precedingEvents: JsonArray,
        override val publishedEvents: JsonArray,
        val exceptionType: String
    ) : LoggedJsonRequest()
}
