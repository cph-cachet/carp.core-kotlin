package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.KSerializer
import kotlin.time.Duration


/**
 * Serializes [Duration] by converting it to microseconds.
 *
 * TODO: Once Kotlin 1.5.30 is released, it will be easy to serialize this as a String rather than Long:
 *   https://github.com/cph-cachet/carp.core-kotlin/issues/287
 */
object DurationSerializer : KSerializer<Duration>
    by createCarpLongPrimitiveSerializer(
        {
            if ( Duration.INFINITE.inWholeMicroseconds == it ) Duration.INFINITE
            else Duration.microseconds( it )
        },
        { it.inWholeMicroseconds }
    )
