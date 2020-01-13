package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.test.JsIgnore
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import kotlin.test.*


/**
 * Tests for [Trigger].
 */
class TriggerTest
{
    @Test
    @JsIgnore
    fun mutable_implementation_triggers_exception()
    {
        class NoDataClass( override val sourceDeviceRoleName: String = "Device" ) : Trigger()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}
