package dk.cachet.carp.protocols.infrastructure.test

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.data.SamplingConfiguration
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistrationBuilder
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


@Serializable
data class StubMasterDeviceDescriptor(
    override val roleName: String = "Stub master device",
    override val samplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()
) : MasterDeviceDescriptor<DefaultDeviceRegistration, DefaultDeviceRegistrationBuilder>()
{
    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DefaultDeviceRegistration> = DefaultDeviceRegistration::class
    override fun isValidConfiguration( registration: DefaultDeviceRegistration ) = Trilean.TRUE
}
