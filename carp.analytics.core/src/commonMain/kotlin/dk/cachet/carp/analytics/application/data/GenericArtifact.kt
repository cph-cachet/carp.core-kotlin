package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.ApplicationData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * A workflow output there is no specific [WorkflowArtifact] type for.
 *
 * This is the last resort: what an output becomes when no manifest, no port descriptor and no
 * recognised MIME type say anything more specific.
 *
 * @param payload The output itself, for values small enough to travel as JSON.
 * @param reference Where the bytes live, for outputs too large to inline.
 * @param schemaRef Reference to the schema the output conforms to, if the step declared one.
 */
@Serializable
@SerialName( DspDataTypes.GENERIC_ARTIFACT_TYPE_NAME )
@JsExport
data class GenericArtifact(
    val payload: ApplicationData? = null,
    val reference: ArtifactReference? = null,
    val schemaRef: String? = null
) : WorkflowArtifact
{
    init
    {
        require( payload != null || reference != null ) { "An artefact needs a payload or a reference." }
    }
}
