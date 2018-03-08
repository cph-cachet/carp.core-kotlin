package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test


/**
 * Tests for [Trigger].
 */
class TriggerTest
{
    @Test
    fun `mutable implementation triggers exception`()
    {
        class NoDataClass( override val sourceDeviceRoleName: String = "Device" ) : Trigger()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}