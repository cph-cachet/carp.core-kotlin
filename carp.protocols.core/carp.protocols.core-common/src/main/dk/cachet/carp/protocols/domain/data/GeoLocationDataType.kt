package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * Geographic location data: longitude and latitude.
 */
@Serializable
data class GeoLocationDataType( override val category: DataCategory = DataCategory.Location ) : DataType()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                GeoLocationDataType::class,
                GeoLocationDataType.serializer(),
                "dk.cachet.carp.protocols.domain.data.GeoLocationDataType" )
        }
    }
}