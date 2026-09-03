package dk.cachet.carp.analytics.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * A stored document produced by a workflow step, e.g. a PDF report.
 *
 * @param reference Where the document is stored.
 * @param title The title of the document.
 */
@Serializable
@SerialName( DspDataTypes.DOCUMENT_ARTIFACT_TYPE_NAME )
@JsExport
data class DocumentArtifact(
    val reference: ArtifactReference,
    val title: String? = null,
) : MediaArtifact
