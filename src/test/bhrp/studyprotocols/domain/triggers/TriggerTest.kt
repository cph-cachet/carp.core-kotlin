package bhrp.studyprotocols.domain.triggers

import bhrp.studyprotocols.domain.InvalidConfigurationError
import bhrp.studyprotocols.domain.devices.DeviceDescriptor
import bhrp.studyprotocols.domain.devices.StubDeviceDescriptor
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