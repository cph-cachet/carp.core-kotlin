package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.serialization.Serializable


/**
 * Geographic location data: longitude and latitude.
 */
@Serializable
data class GeoLocationDataType( override val category: DataCategory = DataCategory.Location ) : DataType()