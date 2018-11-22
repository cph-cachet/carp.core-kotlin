package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.carp.*
import kotlinx.serialization.*


/**
 * Measures any of the sensors typically integrated in smartphones (e.g., accelerometer),
 * or data which is derived from them using vendor-specific APIs (e.g., stepcount, or mode of transport).
 */
@Serializable
data class PhoneSensorMeasure private constructor( override val type: DataType ) : Measure()
{
    companion object
    {
        private val SUPPORTED_DATA_TYPES = arrayOf( GEO_LOCATION, STEPCOUNT )

        init
        {
            PolymorphicSerializer.registerSerializer(
                PhoneSensorMeasure::class,
                PhoneSensorMeasure.serializer(),
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


    init
    {
        // Since supported sensors by CARP should co-evolve across the platform (measure definitions and matching probe implementations),
        // only data types that are supported are allowed. If new probes are implemented for PhoneSensorMeasure, this class should be updated correspondingly.
        // TODO: This is currently 'somewhat' enforced using a private constructor. But, 'copy' can still be used.
        if ( !SUPPORTED_DATA_TYPES.contains( type ) )
        {
            throw IllegalArgumentException( "Invalid data type passed to ${PhoneSensorMeasure::class.simpleName}." )
        }
    }
}