package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.test.JsIgnore
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
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
        class NoDataClass( override val roleName: String = "Not a data class" ) : DeviceDescriptor<DefaultDeviceRegistrationBuilder>()
        {
            override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
            override fun isValidConfiguration( registration: DeviceRegistration ): Trilean = Trilean.TRUE
        }

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}