package carp.protocols.domain.triggers

import carp.protocols.domain.InvalidConfigurationError
import carp.protocols.domain.devices.DeviceDescriptor
import carp.protocols.domain.devices.StubDeviceDescriptor
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
        class NoDataClass( override val sourceDevice: DeviceDescriptor = StubDeviceDescriptor() ) : Trigger()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}