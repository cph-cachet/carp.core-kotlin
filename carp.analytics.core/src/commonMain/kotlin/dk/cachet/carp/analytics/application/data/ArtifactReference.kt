package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Metadata reference to artefacts produced by a workflow step, which are stored separetly from the metadata,
 * e.g. an image in file storage and the metadata.
 *
 * @param uri Where the artefacts are stored.
 * @param mimeType The IANA media type of the bytes, e.g. "image/png".
 * @param sizeBytes Size of the bytes.
 * @param sha256 Lowercase hex SHA-256 digest of the artefact bytes.
 */
@Serializable
@JsExport
@Immutable
@ImplementAsDataClass
data class ArtifactReference(
    val uri: String,
    val mimeType: String,
    val sizeBytes: Long,
    val sha256: String
)
{
    init
    {
        require( uri.isNotBlank() ) { "An artefact reference needs a URI." }
        require( mimeType.isNotBlank() ) { "An artefact reference needs a MIME type." }
        require( sizeBytes >= 0 ) { "Size in bytes cannot be negative." }
        require( sha256Regex.matches( sha256 ) ) { "Expected a lowercase hex SHA-256 digest." }
    }


    companion object
    {
        private val sha256Regex = """^[a-f0-9]{64}$""".toRegex()
    }
}
