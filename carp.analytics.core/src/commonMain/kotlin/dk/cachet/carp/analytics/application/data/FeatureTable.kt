package dk.cachet.carp.analytics.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * A stored table of analytic results, e.g. a parquet file of per-window features.
 *
 * This is the analytic result which does not fit in a data stream. The rows live in storage; this
 * carries the pointer and enough shape to know what is in them without fetching.
 *
 * @param reference Where the table is stored.
 * @param columns The column names, in order.
 * @param rowCount How many rows the table has, if the step reported it.
 */
@Serializable
@SerialName( DspDataTypes.FEATURE_TABLE_TYPE_NAME )
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
data class FeatureTable(
    val reference: ArtifactReference,
    val columns: List<String>,
    val rowCount: Long? = null
) : AnalyticResult
{
    init
    {
        require( columns.isNotEmpty() ) { "A feature table needs at least one column." }
        require( columns.none { it.isBlank() } ) { "Column names cannot be blank." }
        require( rowCount == null || rowCount >= 0 ) { "Row count cannot be negative." }
    }
}
