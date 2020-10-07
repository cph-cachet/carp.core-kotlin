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


    internal const val FREEFORMTEXT_TYPE_NAME = "$CARP_NAMESPACE.freeformtext"
    /**
     * Text of which the interpretation is left up to the specific application.
     */
    val FREEFORMTEXT = add( DataType.fromFullyQualifiedName( FREEFORMTEXT_TYPE_NAME ) )

    internal const val GEOLOCATION_TYPE_NAME = "$CARP_NAMESPACE.geolocation"
    /**
     * Geographic location data, representing latitude and longitude in decimal degrees within the World Geodetic System 1984.
     */
    val GEOLOCATION = add( DataType.fromFullyQualifiedName( GEOLOCATION_TYPE_NAME ) )

    internal const val STEPCOUNT_TYPE_NAME = "$CARP_NAMESPACE.stepcount"
    /**
     * Step count data, representing the number of steps a participant has taken in a specified time interval.
     */
    val STEPCOUNT = add( DataType.fromFullyQualifiedName( STEPCOUNT_TYPE_NAME ) )

    internal const val ECG_TYPE_NAME = "$CARP_NAMESPACE.ecg"
    /**
     * ECG data, representing electrical activity of the heart over time for a single lead.
     */
    val ECG = add( DataType.fromFullyQualifiedName( ECG_TYPE_NAME ) )

    internal const val HEARTRATE_TYPE_NAME = "$CARP_NAMESPACE.heartrate"
    /**
     * Heart rate. Represents the number of heart contractions (beats) per minute.
     */
    val HEARTRATE = add( DataType.fromFullyQualifiedName( HEARTRATE_TYPE_NAME ) )

    internal const val RRI_TYPE_NAME = "$CARP_NAMESPACE.rri"
    /**
     * Heart R to R wave interval in ms.
     */
    val RRI = add( DataType.fromFullyQualifiedName( RRI_TYPE_NAME ) )

    internal const val SENSORCONTACT_TYPE_NAME = "$CARP_NAMESPACE.sensorcontact"
    /**
     * Sensor contact. An indicator of proper skin contact.
     */
    val SENSORCONTACT = add( DataType.fromFullyQualifiedName( SENSORCONTACT_TYPE_NAME ) )

    internal const val ACCELERATION_TYPE_NAME = "$CARP_NAMESPACE.acceleration"
    /**
     * Acceleration in 3 axis.
     */
    val ACCELERATION = add( DataType.fromFullyQualifiedName( ACCELERATION_TYPE_NAME ))
}

