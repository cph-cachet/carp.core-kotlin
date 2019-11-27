package dk.cachet.carp.protocols.domain.data

import kotlinx.serialization.*


/**
 * Defines a type of data which can be processed by the platform (e.g., measured/collected/uploaded).
 * This is used by the infrastructure to determine whether the requested data can be collected on a device,
 * how to upload it, how to process it in a secondary data stream, or how triggers can act on it.
 */
@Serializable
data class DataType(
    /**
     * Uniquely identifies the organization/person who determines how to interpret [name].
     * To prevent conflicts, a reverse domain namespace is suggested: e.g., "org.openmhealth" or "dk.cachet.carp".
     */
    val namespace: String,
    /**
     * Describes the data being collected (e.g., "acceleration", "stepcount", "audio"), but not the sensor (e.g., "accellerometer, "pedometer").
     */
    val name: String )
