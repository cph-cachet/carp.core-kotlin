/**
 * Namespace which contains CARP data type definitions.
 */
package dk.cachet.carp.common.data

import dk.cachet.carp.common.EnumObjectList


/**
 * All CARP [DataType]s.
 */
object CarpDataTypes : EnumObjectList<DataType>()
{
    /**
     * The [DataType] namespace of all CARP data type definitions.
     */
    const val CARP_NAMESPACE: String = "dk.cachet.carp"


    internal const val FREE_FORM_TEXT_TYPE_NAME = "$CARP_NAMESPACE.freeformtext"
    /**
     * Text of which the interpretation is left up to the specific application.
     */
    val FREE_FORM_TEXT = add( DataType.fromFullyQualifiedName( FREE_FORM_TEXT_TYPE_NAME ) )

    internal const val GEOLOCATION_TYPE_NAME = "$CARP_NAMESPACE.geolocation"
    /**
     * Geographic location data, representing latitude and longitude within the World Geodetic System 1984.
     */
    val GEOLOCATION = add( DataType.fromFullyQualifiedName( GEOLOCATION_TYPE_NAME ) )

    internal const val STEP_COUNT_TYPE_NAME = "$CARP_NAMESPACE.stepcount"
    /**
     * Step count data, representing the number of steps a participant has taken in a specified time interval.
     */
    val STEP_COUNT = add( DataType.fromFullyQualifiedName( STEP_COUNT_TYPE_NAME ) )

    internal const val ECG_TYPE_NAME = "$CARP_NAMESPACE.ecg"
    /**
     * Electrocardiography (ECG) data, representing electrical activity of the heart for a single lead.
     */
    val ECG = add( DataType.fromFullyQualifiedName( ECG_TYPE_NAME ) )

    internal const val HEART_RATE_TYPE_NAME = "$CARP_NAMESPACE.heartrate"
    /**
     * Represents the number of heart contractions (beats) per minute.
     */
    val HEART_RATE = add( DataType.fromFullyQualifiedName( HEART_RATE_TYPE_NAME ) )

    internal const val RR_INTERVAL_TYPE_NAME = "$CARP_NAMESPACE.rrinterval"
    /**
     * The time interval between two consecutive heartbeats (R-R interval).
     */
    val RR_INTERVAL = add( DataType.fromFullyQualifiedName( RR_INTERVAL_TYPE_NAME ) )

    internal const val SENSOR_SKIN_CONTACT_TYPE_NAME = "$CARP_NAMESPACE.sensorskincontact"
    /**
     * Determines whether a sensor requiring contact with skin is making proper contact at a specific point in time.
     */
    val SENSOR_SKIN_CONTACT = add( DataType.fromFullyQualifiedName( SENSOR_SKIN_CONTACT_TYPE_NAME ) )

    internal const val ACCELERATION_TYPE_NAME = "$CARP_NAMESPACE.acceleration"
    /**
     * Acceleration along perpendicular x, y, and z axes.
     */
    val ACCELERATION = add( DataType.fromFullyQualifiedName( ACCELERATION_TYPE_NAME ) )
}

