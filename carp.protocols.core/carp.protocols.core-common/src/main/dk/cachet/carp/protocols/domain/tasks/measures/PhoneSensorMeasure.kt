package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.carp.*


/**
 * Measures any of the sensors typically integrated in smartphones (e.g., accelerometer),
 * or data which is derived from them using vendor-specific APIs (e.g., stepcount, or mode of transport).
 */
data class PhoneSensorMeasure( override val type: DataType ) : Measure()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                GeoLocationMeasure::class,
                GeoLocationMeasure.serializer(),
                "dk.cachet.carp.protocols.domain.tasks.measures.PhoneSensorMeasure" )
        }

        fun geolocation(): PhoneSensorMeasure
        {
            return PhoneSensorMeasure( GEO_LOCATION )
        }

        fun stepcount(): PhoneSensorMeasure
        {
            return PhoneSensorMeasure( STEPCOUNT )
        }
    }
}