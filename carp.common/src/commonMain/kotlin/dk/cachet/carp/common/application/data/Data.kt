package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import kotlinx.serialization.*


/**
 * Holds data for a [DataType].
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
interface Data


/**
 * Holds data for a [DataType] collected by a sensor which may include additional [sensorSpecificData].
 */
interface SensorData : Data
{
    /**
     * Additional sensor-specific data pertaining to this data point.
     *
     * This can be used to append highly-specific sensor data to an otherwise common data type.
     */
    val sensorSpecificData: Data?
}


/**
 * Placeholder for generic `Data` types to indicate there is no associated data.
 * This should not be serialized; instead, nullable `Data` should be used.
 */
@Serializable
object NoData : Data
