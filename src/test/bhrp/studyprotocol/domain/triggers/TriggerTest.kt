package bhrp.studyprotocol.domain.triggers

import bhrp.studyprotocol.domain.InvalidConfigurationError
import bhrp.studyprotocol.domain.devices.DeviceDescriptor
import bhrp.studyprotocol.domain.devices.StubDeviceDescriptor
import org.junit.jupiter.api.*
import kotlin.test.*


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