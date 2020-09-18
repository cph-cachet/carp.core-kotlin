/**
 * Namespace which contains CARP data type definitions.
 */
package dk.cachet.carp.common.data


/**
 * The [DataType] namespace of all CARP data type definitions.
 */
const val CARP_NAMESPACE: String = "dk.cachet.carp"

/**
 * Get a [DataType] definition with the given [name] in the [CARP_NAMESPACE].
 */
private fun carpDataType( name: String ) = DataType( CARP_NAMESPACE, name )


/**
 * Geographic location data, representing longitude and latitude.
 */
val GEOLOCATION_TYPE = carpDataType( "geolocation" )

/**
 * Stepcount data, representing the number of steps a participant has taken in a specified time interval.
 */
val STEPCOUNT_TYPE = carpDataType( "stepcount" )