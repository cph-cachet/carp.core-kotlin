package dk.cachet.carp.protocols.domain.data

import kotlinx.serialization.Serializable


/**
 * Geographic location data: longitude and latitude.
 */
@Serializable
data class GeoLocationDataType( override val category: DataCategory = DataCategory.Location ) : DataType()