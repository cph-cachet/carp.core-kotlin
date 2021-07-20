package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import kotlinx.serialization.Serializable


/**
 * Identifies a data stream of collected [dataType] data on the device with [deviceRoleName]
 * in a deployed study protocol with [studyDeploymentId].
 */
@Serializable
data class DataStreamId( val studyDeploymentId: UUID, val deviceRoleName: String, val dataType: DataType )
