package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.ApplicationData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * A rendered plot, optionally with the spec it was rendered from.
 *
 * The rendered image is always a reference. The spec travels inline because it is small and a
 * client which understands [specFormat] can re-render the plot from it without a second fetch.
 *
 * @param reference Where the rendered image is stored.
 * @param spec The plot definition, e.g. a Vega-Lite spec.
 * @param specFormat What [spec] is written in, e.g. "vega-lite/v5". Required when [spec] is given.
 * @param title A caption for the plot.
 */
@Serializable
@SerialName( DspDataTypes.PLOT_ARTIFACT_TYPE_NAME )
@JsExport
data class PlotArtifact(
    val reference: ArtifactReference,
    val spec: ApplicationData? = null,
    val specFormat: String? = null,
    val title: String? = null
) : MediaArtifact
{
    init
    {
        require( spec == null || !specFormat.isNullOrBlank() ) { "A plot spec needs to say what format it is in." }
    }
}
