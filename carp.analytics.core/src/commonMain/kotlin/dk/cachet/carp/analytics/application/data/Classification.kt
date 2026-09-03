package dk.cachet.carp.analytics.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * The [label] a model assigned to a window of data.
 *
 * @param label The assigned class, e.g. "sleep".
 * @param confidence How certain the model is, from 0 to 1, if it reports this.
 * @param modelRef Identifies the model and version which assigned [label], for reproducibility.
 */
@Serializable
@SerialName( DspDataTypes.CLASSIFICATION_TYPE_NAME )
@JsExport
data class Classification(
    val label: String,
    val confidence: Double? = null,
    val modelRef: String? = null
) : AnalyticResult
{
    init
    {
        require( label.isNotBlank() ) { "A classification needs a label." }
        require( confidence == null || confidence in 0.0..1.0 ) { "Confidence needs to lie between 0 and 1." }
    }
}
