package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*


/**
 * Serializes [String] as a [JsonElement], but only in case a JSON encoder/decoder is used.
 *
 * This is useful to store application-specific data which is not statically known to a common base infrastructure
 * when JSON serialization is used, without having to escape the JSON data.
 *
 * In case the JSON contained in the String is malformed, it will be serialized as a normal escaped string.
 */
@OptIn( ExperimentalSerializationApi::class )
class ApplicationDataSerializer : KSerializer<String?>
{
    override val descriptor: SerialDescriptor = String.serializer().nullable.descriptor

    override fun deserialize( decoder: Decoder ): String?
    {
        // Early out when the value is null.
        if ( !decoder.decodeNotNullMark() ) return null

        // Application data is only serialized as JSON for JSON encoder.
        if ( decoder !is JsonDecoder ) return decoder.decodeNullableSerializableValue( String.serializer().nullable )

        // Read application data which is stored as JSON.
        val jsonElement = decoder.decodeJsonElement()
        val originalString = jsonElement.toString()

        // In case application data was a primitive string, trim the surrounding quotes.
        return if ( jsonElement is JsonObject ) originalString
        else originalString.substring( 1, originalString.length - 1 )
    }

    override fun serialize( encoder: Encoder, value: String? )
    {
        // Early out when the value is null.
        if ( value == null )
        {
            encoder.encodeNull()
            return
        }

        // Application data is only serialized as JSON for JSON encoder.
        if ( encoder !is JsonEncoder )
        {
            encoder.encodeNullableSerializableValue( String.serializer().nullable, value )
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
