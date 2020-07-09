package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.test.JsIgnore
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.SamplingConfiguration
import kotlin.reflect.KClass
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
        class NoDataClass( override val roleName: String = "Not a data class" ) :
            DeviceDescriptor<DefaultDeviceRegistration, DefaultDeviceRegistrationBuilder>()
        {
            override val samplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

            override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
            override fun getRegistrationClass(): KClass<DefaultDeviceRegistration> = DefaultDeviceRegistration::class
            override fun isValidConfiguration( registration: DefaultDeviceRegistration ): Trilean = Trilean.TRUE
        }

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}
