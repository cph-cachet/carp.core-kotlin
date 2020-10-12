package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.sampling.SamplingConfiguration
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


@Serializable
data class BLEHeartRateSensor( override val roleName: String )
    : DeviceDescriptor<MACAddressDeviceRegistration, MACAddressDeviceRegistrationBuilder>()
{
    override val supportedDataTypes: Set<DataType> = emptySet()

    override val samplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): MACAddressDeviceRegistrationBuilder = MACAddressDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<MACAddressDeviceRegistration> = MACAddressDeviceRegistration::class
    override fun isValidConfiguration( registration: MACAddressDeviceRegistration ): Trilean = Trilean.TRUE
}
