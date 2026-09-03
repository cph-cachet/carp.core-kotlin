package dk.cachet.carp.analytics.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * A statistic with [name] describing a set of [sampleCount] observations, e.g. "mean" over a day.
 *
 * [sampleCount] is what separates this from a [ComputedFeature]: a summary statistic is only
 * interpretable if you know how many observations it describes. Report a value without that as a
 * [ComputedFeature].
 *
 * @param name The statistic, e.g. "mean", "sd", "median".
 * @param value The computed value.
 * @param sampleCount How many observations [value] describes.
 * @param unit The unit [value] is expressed in, e.g. "bpm", if it has one.
 */
@Serializable
@SerialName( DspDataTypes.SUMMARY_STATISTIC_TYPE_NAME )
@JsExport
data class SummaryStatistic(
    val name: String,
    val value: Double,
    val sampleCount: Int,
    val unit: String? = null
) : AnalyticResult
{
    init
    {
        require( name.isNotBlank() ) { "A summary statistic needs a name." }
        require( sampleCount > 0 ) { "A summary statistic needs at least one observation." }
    }
}
