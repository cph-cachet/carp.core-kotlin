/**
 * Namespace which contains DSP workflow artefact data type definitions.
 */
package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.data.DataTimeType
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.data.DataTypeMetaData
import dk.cachet.carp.common.application.data.DataTypeMetaDataMap
import kotlin.js.JsExport


/**
 * All [DataType]s produced by DSP workflows.
 *
 * This is the one place the full set is enumerable. A study protocol has to declare every type a
 * workflow may emit, so adding a member here is a protocol-affecting change.
 *
 * The namespace encodes which [WorkflowArtifact] branch a type belongs to, so consumers which only
 * see type strings can filter by prefix. Use [typesIn].
 */
@JsExport
object DspDataTypes : DataTypeMetaDataMap()
{
    /**
     * The [DataType] namespace of all DSP workflow artefact definitions.
     */
    const val DSP_NAMESPACE: String = "dk.cachet.carp.dsp"

    /**
     * The namespace of every [AnalyticResult].
     */
    const val ANALYTIC_NAMESPACE: String = "$DSP_NAMESPACE.analytic"

    /**
     * The namespace of every [MediaArtifact].
     */
    const val MEDIA_NAMESPACE: String = "$DSP_NAMESPACE.media"

    /**
     * The namespace of every [ControlSignal].
     */
    const val CONTROL_NAMESPACE: String = "$DSP_NAMESPACE.control"


    internal const val GENERIC_ARTIFACT_TYPE_NAME = "$DSP_NAMESPACE.generic"
    /**
     * A workflow output there is no specific type for. It has no branch.
     */
    val GENERIC_ARTIFACT = add(
        GENERIC_ARTIFACT_TYPE_NAME,
        "Generic workflow artefact",
        DataTimeType.POINT
    )


    internal const val COMPUTED_FEATURE_TYPE_NAME = "$ANALYTIC_NAMESPACE.feature"
    /**
     * A single named value computed over a window of data. See [ComputedFeature].
     */
    val COMPUTED_FEATURE = add(
        COMPUTED_FEATURE_TYPE_NAME,
        "Computed feature",
        DataTimeType.TIME_SPAN
    )

    internal const val SUMMARY_STATISTIC_TYPE_NAME = "$ANALYTIC_NAMESPACE.summary"
    /**
     * A statistic describing a set of observations. See [SummaryStatistic].
     */
    val SUMMARY_STATISTIC = add(
        SUMMARY_STATISTIC_TYPE_NAME,
        "Summary statistic",
        DataTimeType.TIME_SPAN
    )

    internal const val CLASSIFICATION_TYPE_NAME = "$ANALYTIC_NAMESPACE.classification"
    /**
     * A label assigned to a window of data by a model. See [Classification].
     */
    val CLASSIFICATION = add(
        CLASSIFICATION_TYPE_NAME,
        "Classification",
        DataTimeType.TIME_SPAN
    )

    internal const val FEATURE_TABLE_TYPE_NAME = "$ANALYTIC_NAMESPACE.table"
    /**
     * A stored table of analytic results, too large to travel inline. See [FeatureTable].
     */
    val FEATURE_TABLE = add(
        FEATURE_TABLE_TYPE_NAME,
        "Feature table",
        DataTimeType.TIME_SPAN
    )


    internal const val IMAGE_ARTIFACT_TYPE_NAME = "$MEDIA_NAMESPACE.image"
    /**
     * A stored image. See [ImageArtifact].
     */
    val IMAGE_ARTIFACT = add(
        IMAGE_ARTIFACT_TYPE_NAME,
        "Image artefact",
        DataTimeType.POINT
    )

    internal const val PLOT_ARTIFACT_TYPE_NAME = "$MEDIA_NAMESPACE.plot"
    /**
     * A rendered plot, optionally with the spec it was rendered from. See [PlotArtifact].
     */
    val PLOT_ARTIFACT = add(
        PLOT_ARTIFACT_TYPE_NAME,
        "Plot artefact",
        DataTimeType.POINT
    )

    internal const val DOCUMENT_ARTIFACT_TYPE_NAME = "$MEDIA_NAMESPACE.document"
    /**
     * A stored document, e.g. a PDF report. See [DocumentArtifact].
     */
    val DOCUMENT_ARTIFACT = add(
        DOCUMENT_ARTIFACT_TYPE_NAME,
        "Document artefact",
        DataTimeType.POINT
    )


    internal const val TRIGGER_ACTIVATION_TYPE_NAME = "$CONTROL_NAMESPACE.trigger"
    /**
     * The record of a workflow activating a study protocol trigger. See [TriggerActivation].
     */
    val TRIGGER_ACTIVATION = add(
        TRIGGER_ACTIVATION_TYPE_NAME,
        "Trigger activation",
        DataTimeType.POINT
    )


    /**
     * All data types in [namespace], e.g. [ANALYTIC_NAMESPACE].
     *
     * Passing [DSP_NAMESPACE] returns every DSP type, including those in the branches below it.
     */
    @Suppress( "NON_EXPORTABLE_TYPE" )
    fun typesIn( namespace: String ): List<DataTypeMetaData> =
        values.filter { it.type.toString().startsWith( "$namespace." ) }
}
