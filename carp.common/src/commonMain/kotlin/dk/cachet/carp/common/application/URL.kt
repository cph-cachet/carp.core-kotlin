package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createCarpStringPrimitiveSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * TODO
 *
 * @param stringRepresentation The string representation of the URL.
 */
@Serializable( URLSerializer::class )
@JsExport
class URL ( val stringRepresentation: String )
{
    init
    {
        require( URIRegex.matches( stringRepresentation ) ) { "Invalid URI string representation." }
    }
}

/**
 * Regular expression to match any URI according to
 * [Appendix B of RFC 3986/STD 0066](https://www.rfc-editor.org/rfc/rfc3986#appendix-B)
 *
 * TODO: [URIRegex] allows us to write our own validation
 */
val URIRegex = Regex("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?")

/**
 * A custom serializer for [URL].
 */
object URLSerializer : KSerializer<URL> by createCarpStringPrimitiveSerializer( { URL( it ) } )

