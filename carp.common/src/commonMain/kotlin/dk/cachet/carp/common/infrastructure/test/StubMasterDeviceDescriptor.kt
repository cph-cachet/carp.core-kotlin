package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistrationBuilder
import dk.cachet.carp.common.application.devices.MasterDeviceDescriptor
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * A stub [MasterDeviceDescriptor] which can measure [STUB_DATA_TYPE].
 */
@Serializable
data class StubMasterDeviceDescriptor(
    override val roleName: String = "Stub master device",
    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap(),
    override val supportedDataTypes: Set<DataType> = setOf( STUB_DATA_TYPE )
) : MasterDeviceDescriptor<DefaultDeviceRegistration, DefaultDeviceRegistrationBuilder>()
{
    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DefaultDeviceRegistration> = DefaultDeviceRegistration::class
    override fun isValidConfiguration( registration: DefaultDeviceRegistration ) = Trilean.TRUE
}
