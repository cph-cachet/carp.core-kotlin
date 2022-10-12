package dk.cachet.carp.common.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds photoplethysmogram (PPG) data collected by a photodetector,
 * measuring reflected light coming from one or more individually emitting [lightSources].
 */
@Serializable
@SerialName( CarpDataTypes.PPG_TYPE_NAME )
data class PPG(
    /**
     * A collection of light sources for which a photodetector collected data, identified by name (key),
     * and the amount of measured reflected light (value).
     * The unit of the received data by the photodetector is determined by the sensor manufacturer.
     */
    val lightSources: Map<String, Double>
) : Data
{
    init { require( lightSources.isNotEmpty() ) { "Data for at least one light sources needs to be provided." } }
}
