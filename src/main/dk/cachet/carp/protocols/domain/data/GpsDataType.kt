package dk.cachet.carp.protocols.domain.data

import kotlinx.serialization.Serializable


/**
 * Global Positioning System (GPS) data, comprising longitude and latitude.
 */
@Serializable
data class GpsDataType( override val category: DataCategory = DataCategory.Location ) : DataType()