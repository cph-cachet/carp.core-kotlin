package dk.cachet.carp.protocols.infrastructure.test

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.sampling.SamplingConfiguration
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistrationBuilder
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * A stub [MasterDeviceDescriptor] which can measure [STUB_DATA_TYPE].
 */
@Serializable
data class StubMasterDeviceDescriptor(
    override val roleName: String = "Stub master device",
    override val samplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap(),
    override val supportedDataTypes: Set<DataType> = setOf( STUB_DATA_TYPE )
) : MasterDeviceDescriptor<DefaultDeviceRegistration, DefaultDeviceRegistrationBuilder>()
{
    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DefaultDeviceRegistration> = DefaultDeviceRegistration::class
    override fun isValidConfiguration( registration: DefaultDeviceRegistration ) = Trilean.TRUE
}
