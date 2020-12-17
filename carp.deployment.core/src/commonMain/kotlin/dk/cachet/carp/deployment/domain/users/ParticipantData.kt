package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.InputDataType
import kotlinx.serialization.Serializable


/**
 * Set [data] for all expected participant data in the study deployment with [studyDeploymentId].
 * Data which is not set equals null.
 */
@Serializable
data class ParticipantData( val studyDeploymentId: UUID, val data: Map<InputDataType, Data?> )
