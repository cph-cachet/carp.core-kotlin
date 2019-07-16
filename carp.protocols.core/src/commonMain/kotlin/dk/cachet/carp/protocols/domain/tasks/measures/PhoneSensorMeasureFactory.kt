package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.TimeSpan


/**
 * Contains factory methods to initialize phone sensor measures.
 */
interface PhoneSensorMeasureFactory
{
    /**
     * Measure geographic location data (longitude and latitude).
     */
    fun geolocation( duration: TimeSpan = TimeSpan.INFINITE ): Measure

    /**
     * Measure amount of steps a participant has taken (measured per time interval).
     */
    fun stepcount( duration: TimeSpan = TimeSpan.INFINITE ): Measure
}