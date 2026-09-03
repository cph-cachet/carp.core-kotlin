package dk.cachet.carp.analytics.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * A stored image produced by a workflow step.
 *
 * @param reference Where the image is stored.
 * @param widthPixels Width of the image, if the step reported it.
 * @param heightPixels Height of the image, if the step reported it.
 */
@Serializable
@SerialName( DspDataTypes.IMAGE_ARTIFACT_TYPE_NAME )
@JsExport
data class ImageArtifact(
    val reference: ArtifactReference,
    val widthPixels: Int? = null,
    val heightPixels: Int? = null
) : MediaArtifact
{
    init
    {
        require( widthPixels == null || widthPixels > 0 ) { "Image width needs to be positive." }
        require( heightPixels == null || heightPixels > 0 ) { "Image height needs to be positive." }
    }
}
