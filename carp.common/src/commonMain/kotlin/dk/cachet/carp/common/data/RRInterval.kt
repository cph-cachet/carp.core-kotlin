package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * Indicates the corresponding time interval represents the time between two consecutive heartbeats (R-R interval).
 */
@Serializable
@SerialName( CarpDataTypes.RR_INTERVAL_TYPE_NAME )
data class RRInterval private constructor(
    // HACK: Ideally, RRInterval is turned into an object and not a data class.
    // This dummy field is added to circumvent a bug in kotlinx.serialization on the JS target platform:
    // https://github.com/Kotlin/kotlinx.serialization/issues/1138
    @Transient private val ignoreThis: Unit = Unit
) : Data
{
    constructor() : this( Unit )
}
