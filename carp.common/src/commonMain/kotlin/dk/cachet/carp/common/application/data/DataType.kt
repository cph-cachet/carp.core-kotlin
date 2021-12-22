package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.application.NamespacedId
import kotlin.js.JsExport


/**
 * Defines a type of data which can be processed by the platform (e.g., measured/collected/uploaded).
 * This is used by the infrastructure to determine whether the requested data can be collected on a device,
 * how to upload it, how to process it in a secondary data stream, or how triggers can act on it.
 */
typealias DataType = NamespacedId


/**
 * Contains metadata about [type].
 */
@JsExport
data class DataTypeMetaData(
    /**
     * Unique fully qualified name for the data type this meta data relates to.
     */
    val type: DataType,
    /**
     * A name which can be used to display to the user which data is collected.
     */
    val displayName: String,
    /**
     * Determines how [Data] for [type] should be stored temporally.
     */
    val timeType: DataTimeType
)


/**
 * Describes how [Data] for a [DataType] should be stored temporally.
 */
enum class DataTimeType
{
    /**
     * Data is related to one specific point in time.
     */
    POINT,

    /**
     * Data is related to a period of time between two specific point in time.
     */
    TIME_SPAN,

    /**
     * Data can be either related to one specific point in time ([POINT]), or a [TIME_SPAN].
     */
    EITHER;


    /**
     * Determines whether this [DataTimeType] matches the [required] [DataTimeType].
     */
    fun matches( required: DataTimeType ): Boolean = required == EITHER || this == required
}
