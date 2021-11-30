/**
 * Namespace which contains CARP data type definitions.
 */
package dk.cachet.carp.common.application.data


/**
 * All CARP data types.
 */
object CarpDataTypes : DataTypeMetaDataMap()
{
    /**
     * The [DataType] namespace of all CARP data type definitions.
     */
    const val CARP_NAMESPACE: String = "dk.cachet.carp"


    internal const val FREE_FORM_TEXT_TYPE_NAME = "$CARP_NAMESPACE.freeformtext"
    /**
     * Text of which the interpretation is left up to the specific application.
     */
    val FREE_FORM_TEXT = add( FREE_FORM_TEXT_TYPE_NAME, "Application-specific data", DataTimeType.EITHER )

    internal const val GEOLOCATION_TYPE_NAME = "$CARP_NAMESPACE.geolocation"
    /**
     * Geographic location data, representing latitude and longitude within the World Geodetic System 1984.
     */
    val GEOLOCATION = add( GEOLOCATION_TYPE_NAME, "Geolocation", DataTimeType.POINT )

    internal const val STEP_COUNT_TYPE_NAME = "$CARP_NAMESPACE.stepcount"
    /**
     * Step count data, representing the number of steps a participant has taken in a specified time interval.
     */
    val STEP_COUNT = add( STEP_COUNT_TYPE_NAME, "Step count", DataTimeType.TIME_SPAN )

    internal const val ECG_TYPE_NAME = "$CARP_NAMESPACE.ecg"
    /**
     * Electrocardiography (ECG) data, representing electrical activity of the heart for a single lead.
     */
    val ECG = add( ECG_TYPE_NAME, "Electrocardiography (ECG)", DataTimeType.POINT )

    internal const val HEART_RATE_TYPE_NAME = "$CARP_NAMESPACE.heartrate"
    /**
     * Represents the number of heart contractions (beats) per minute.
     */
    val HEART_RATE = add( HEART_RATE_TYPE_NAME, "Heart rate", DataTimeType.POINT )

    internal const val RR_INTERVAL_TYPE_NAME = "$CARP_NAMESPACE.rrinterval"
    /**
     * The time interval between two consecutive heartbeats (R-R interval).
     */
    val RR_INTERVAL = add( RR_INTERVAL_TYPE_NAME, "R-R interval", DataTimeType.TIME_SPAN )

    internal const val SENSOR_SKIN_CONTACT_TYPE_NAME = "$CARP_NAMESPACE.sensorskincontact"
    /**
     * Determines whether a sensor requiring contact with skin is making proper contact at a specific point in time.
     */
    val SENSOR_SKIN_CONTACT = add( SENSOR_SKIN_CONTACT_TYPE_NAME, "Sensor skin contact", DataTimeType.POINT )

    internal const val ACCELERATION_TYPE_NAME = "$CARP_NAMESPACE.acceleration"
    /**
     * Acceleration along perpendicular x, y, and z axes.
     */
    val ACCELERATION = add( ACCELERATION_TYPE_NAME, "Accelerometry", DataTimeType.POINT )

    internal const val SIGNAL_STRENGTH_TYPE_NAME = "$CARP_NAMESPACE.signalstrength"
    /**
     * The received signal strength of a wireless device.
     */
    val SIGNAL_STRENGTH = add( SIGNAL_STRENGTH_TYPE_NAME, "Signal strength", DataTimeType.POINT )

    internal const val TRIGGERED_TASK_TYPE_NAME = "$CARP_NAMESPACE.triggeredtask"
    /**
     * A task which was started or stopped by a trigger, referring to identifiers in the study protocol.
     */
    val TRIGGERED_TASK = add( TRIGGERED_TASK_TYPE_NAME, "Triggered task", DataTimeType.POINT )

    internal const val COMPLETED_TASK_TYPE_NAME = "$CARP_NAMESPACE.completedtask"
    /**
     * An interactive task which was completed over the course of a specified time interval.
     */
    val COMPLETED_TASK = add( COMPLETED_TASK_TYPE_NAME, "Completed task", DataTimeType.TIME_SPAN )
}
