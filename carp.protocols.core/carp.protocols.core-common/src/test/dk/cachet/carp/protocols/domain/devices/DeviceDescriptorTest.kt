package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.test.JsIgnore
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
        {
            override fun createRegistration(): DeviceRegistration = defaultDeviceRegistration()
            override fun isValidConfiguration( registration: DeviceRegistration ): Trilean = Trilean.TRUE
        }

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}