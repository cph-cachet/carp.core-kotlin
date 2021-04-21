package dk.cachet.carp.common.application.tasks.measures

import dk.cachet.carp.common.application.TimeSpan
import dk.cachet.carp.common.application.data.DataType
import kotlinx.serialization.Serializable


/**
 * Measures any of the sensors typically integrated in smartphones (e.g., accelerometer),
 * or data which is derived from them using vendor-specific APIs (e.g., stepcount, or mode of transport).
 */
@Suppress( "DataClassPrivateConstructor" )
@Serializable
data class PhoneSensorMeasure(
    override val type: DataType,
    /**
     * The optional duration over the course of which the sensor identified by [type] needs to be measured.
     * Infinite by default.
     */
    val duration: TimeSpan = TimeSpan.INFINITE
) : Measure
