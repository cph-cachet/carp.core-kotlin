package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject


/**
 * Serializes [String] as a [JsonElement], but only in case a JSON encoder/decoder is used.
 *
 * This is useful to store application-specific data which is not statically known to a common base infrastructure
 * when JSON serialization is used, without having to escape the JSON data.

 * In case the JSON contained in the String is malformed, it will be serialized as a normal escaped string.
 */
class ApplicationDataSerializer : KSerializer<String>
{
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize( decoder: Decoder ): String
    {
        // Application data is only serialized as JSON for JSON encoder.
        if ( decoder !is JsonDecoder ) return decoder.decodeString()

        // Read application data which is stored as JSON.
        val jsonElement = decoder.decodeJsonElement()
        val originalString = jsonElement.toString()

        // In case application data was a primitive string, trim the surrounding quotes.
        return if ( jsonElement is JsonObject ) originalString
        else originalString.substring( 1, originalString.length - 1 )
    }

    override fun serialize( encoder: Encoder, value: String )
    {
        // Application data is only serialized as JSON for JSON encoder.
        if ( encoder !is JsonEncoder )
        {
            encoder.encodeString( value )
            return
        }

        val json = encoder.json
        var isJsonObject = value.startsWith( "{" )
        if ( isJsonObject )
        {
            try
            {
                val jsonElement = json.parseToJsonElement( value )
                encoder.encodeJsonElement( jsonElement )
            }
            catch( _: SerializationException )
            {
                isJsonObject = false
            }
        }

        if ( !isJsonObject ) encoder.encodeString( value )
    }
}
