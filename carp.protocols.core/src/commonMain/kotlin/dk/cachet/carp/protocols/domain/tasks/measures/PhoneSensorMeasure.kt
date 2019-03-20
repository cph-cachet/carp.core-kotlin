package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.carp.*
import kotlinx.serialization.*


/**
 * Measures any of the sensors typically integrated in smartphones (e.g., accelerometer),
 * or data which is derived from them using vendor-specific APIs (e.g., stepcount, or mode of transport).
 */
@Serializable
data class PhoneSensorMeasure private constructor(
    override val type: DataType,
    /**
     * The optional duration over the course of which the sensor identified by [type] needs to be measured.
     * Infinite by default.
     */
    val duration: TimeSpan = TimeSpan.INFINITE ) : Measure()
{
    companion object Factory : PhoneSensorMeasureFactory
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                PhoneSensorMeasure::class,
                PhoneSensorMeasure.serializer(),
                "dk.cachet.carp.protocols.domain.tasks.measures.PhoneSensorMeasure" )
        }

        private val SUPPORTED_DATA_TYPES = arrayOf( GEO_LOCATION, STEPCOUNT )

        override fun geolocation( duration: TimeSpan ) = PhoneSensorMeasure( GEO_LOCATION, duration )
        override fun stepcount( duration: TimeSpan ) = PhoneSensorMeasure( STEPCOUNT, duration )
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