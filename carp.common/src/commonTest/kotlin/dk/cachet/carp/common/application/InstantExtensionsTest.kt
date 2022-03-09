package dk.cachet.carp.common.application

import kotlinx.datetime.Instant
import kotlin.test.*
import kotlin.time.Duration.Companion.microseconds


class InstantExtensionsTest
{
    @Test
    fun toEpochMicroseconds_succeeds()
    {
        val oneMillisecond = Instant.fromEpochMilliseconds( 1 )
        assertEquals( 1000, oneMillisecond.toEpochMicroseconds() )

        val oneMicrosecond = Instant.fromEpochMilliseconds( 0 )
            .plus( 1.microseconds )
        assertEquals( 1, oneMicrosecond.toEpochMicroseconds() )
    }
}
