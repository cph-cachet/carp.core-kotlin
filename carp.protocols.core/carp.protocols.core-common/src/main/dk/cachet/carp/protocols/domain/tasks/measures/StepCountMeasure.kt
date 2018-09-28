package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import kotlinx.serialization.Serializable


/**
 * Measure the amount of steps taken as assessed by the device this measure is requested on.
 */
@Serializable
data class StepCountMeasure( override val type: DataType = StepCountDataType() ) : DataStreamMeasure()