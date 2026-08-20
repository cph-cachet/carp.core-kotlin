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


    /**
     * All data types in [namespace], e.g. [ANALYTIC_NAMESPACE].
     *
     * Passing [DSP_NAMESPACE] returns every DSP type, including those in the branches below it.
     */
    @Suppress( "NON_EXPORTABLE_TYPE" )
    fun typesIn( namespace: String ): List<DataTypeMetaData> =
        values.filter { it.type.toString().startsWith( "$namespace." ) }
}
