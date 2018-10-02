package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * Measure the geographic location (longitude and latitude) as determined by the device this measure is requested on.
 */
@Serializable
data class GeoLocationMeasure(
    @Serializable( PolymorphicSerializer::class )
    override val type: DataType = GeoLocationDataType() ) : DataStreamMeasure()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( GeoLocationMeasure::class, "dk.cachet.carp.protocols.domain.tasks.measures.GeoLocationMeasure" ) }
    }
}