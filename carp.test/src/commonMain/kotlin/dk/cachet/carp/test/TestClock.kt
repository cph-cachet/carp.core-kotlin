package dk.cachet.carp.test

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


/**
 * A fixed clock at UNIX epoch for testing with deterministic behavior.
 */
object TestClock : Clock
{
    private val currentInstant: Instant = Instant.fromEpochSeconds( 0 )

    override fun now(): Instant = currentInstant
}
