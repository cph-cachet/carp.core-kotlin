package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * Geographic location data: longitude and latitude.
 */
@Serializable
data class GeoLocationDataType( override val category: DataCategory = DataCategory.Location ) : DataType()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( GeoLocationDataType::class, "dk.cachet.carp.protocols.domain.data.GeoLocationDataType" ) }
    }
}