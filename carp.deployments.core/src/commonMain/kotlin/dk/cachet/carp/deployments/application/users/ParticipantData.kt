@file:JsExport

package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Set [data] for all expected participant data in the study deployment with [studyDeploymentId].
 * Data which is not set equals null.
 */
@Serializable
data class ParticipantData( val studyDeploymentId: UUID, val data: Map<InputDataType, Data?> )
