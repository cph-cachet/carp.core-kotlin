/**
 * Namespace which contains CARP supported data type definitions.
 */
package dk.cachet.carp.protocols.domain.data.carp

import dk.cachet.carp.protocols.domain.data.DataType


const val CARP_NAMESPACE: String = "dk.cachet.carp"

/**
 * Geographic location data: longitude and latitude.
 */
val GEO_LOCATION: DataType = DataType( CARP_NAMESPACE, "geo_location" )
/**
 * Amount of steps a participant has taken in a specified time interval.
 */
val STEPCOUNT: DataType = DataType( CARP_NAMESPACE, "stepcount" )