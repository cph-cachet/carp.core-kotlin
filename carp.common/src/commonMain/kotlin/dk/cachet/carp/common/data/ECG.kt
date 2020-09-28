package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName( ECG_TYPE_NAME )
data class ECG( val voltage: Double ): Data
