package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.application.NamespacedId


/**
 * Defines a type of data which can be processed by the platform (e.g., measured/collected/uploaded).
 * This is used by the infrastructure to determine whether the requested data can be collected on a device,
 * how to upload it, how to process it in a secondary data stream, or how triggers can act on it.
 */
typealias DataType = NamespacedId


/**
 * Contains metadata about [type].
 */
data class DataTypeMetaData(
    /**
     * Unique fully qualified name for the data type this meta data relates to.
     */
    val type: DataType,
    /**
     * A name which can be used to display to the user which data is collected.
     */
    val displayName: String
)
