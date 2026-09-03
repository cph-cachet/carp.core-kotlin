package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Instant


/**
 * Tests for the [ControlSignal] leaves.
 */
class ControlSignalTest
{
    @Test
    fun trigger_id_cant_be_negative()
    {
        assertFailsWith<IllegalArgumentException>
        {
            TriggerActivation( -1, UUID.randomUUID(), Instant.fromEpochSeconds( 0 ) )
        }
    }
}
