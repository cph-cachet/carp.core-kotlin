/**
 * Namespace which contains CARP data type definitions.
 */
package dk.cachet.carp.common.data


/**
 * The [DataType] namespace of all CARP data type definitions.
 */
const val CARP_NAMESPACE: String = "dk.cachet.carp"


internal const val FREEFORMTEXT_TYPE_NAME = "$CARP_NAMESPACE.freeformtext"
/**
 * Text of which the interpretation is left up to the specific application.
 */
val FREEFORMTEXT_TYPE = DataType.fromFullyQualifiedName( FREEFORMTEXT_TYPE_NAME )

internal const val GEOLOCATION_TYPE_NAME = "$CARP_NAMESPACE.geolocation"
/**
 * Geographic location data, representing longitude and latitude.
 */
val GEOLOCATION_TYPE = DataType.fromFullyQualifiedName( GEOLOCATION_TYPE_NAME )

internal const val STEPCOUNT_TYPE_NAME = "$CARP_NAMESPACE.stepcount"
/**
 * Stepcount data, representing the number of steps a participant has taken in a specified time interval.
 */
val STEPCOUNT_TYPE = DataType.fromFullyQualifiedName( STEPCOUNT_TYPE_NAME )

internal const val ECG_TYPE_NAME = "$CARP_NAMESPACE.ecg"
/**
 * ECG data, representing electrical activity of the heart over time.
 */
val ECG_TYPE = DataType.fromFullyQualifiedName( ECG_TYPE_NAME )

internal const val HEARTRATE_TYPE_NAME = "$CARP_NAMESPACE.heartrate"
/**
 * Heart rate. Measures the number of heart contractions (beats) per minute.
 */
val HEARTRATE_TYPE = DataType.fromFullyQualifiedName( HEARTRATE_TYPE_NAME )
