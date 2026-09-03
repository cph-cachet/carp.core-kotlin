package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant


/**
 * The record of a workflow activating the study protocol trigger with [triggerId].
 *
 * The activation itself is a service call. This is the auditable trace it leaves behind, so it
 * repeats the deployment and the time rather than relying on an envelope: artefacts also travel
 * through the artefact registry, which is keyed by workflow run and has neither.
 *
 * @param triggerId The trigger in the study protocol which was activated.
 * @param studyDeploymentId The deployment the trigger was activated for.
 * @param activatedAt When the activation was requested.
 */
@Serializable
@SerialName( DspDataTypes.TRIGGER_ACTIVATION_TYPE_NAME )
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
data class TriggerActivation(
    val triggerId: Int,
    val studyDeploymentId: UUID,
    val activatedAt: Instant
) : ControlSignal
{
    init
    {
        require( triggerId >= 0 ) { "A trigger ID cannot be negative." }
    }
}
