package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.*
import kotlin.test.*


/**
 * Tests for [DeviceDescriptor].
 */
class DeviceDescriptorTest
{
    @Test
    @JsIgnore
    fun mutable_implementation_triggers_exception()
    {
        class NoDataClass( override val roleName: String = "Not a data class" ) : DeviceDescriptor()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}