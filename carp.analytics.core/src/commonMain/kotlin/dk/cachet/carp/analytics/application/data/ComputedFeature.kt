package dk.cachet.carp.analytics.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * A single value with [name] computed over a window of data, e.g. "hr_mean".
 *
 * @param name Identifies the feature within the workflow which produced it.
 * @param value The computed value.
 * @param unit The unit [value] is expressed in, e.g. "bpm", if it has one.
 */
@Serializable
@SerialName( DspDataTypes.COMPUTED_FEATURE_TYPE_NAME )
@JsExport
data class ComputedFeature(
    val name: String,
    val value: Double,
    val unit: String? = null
) : AnalyticResult
{
    init
    {
        require( name.isNotBlank() ) { "A feature needs a name." }
    }
}
