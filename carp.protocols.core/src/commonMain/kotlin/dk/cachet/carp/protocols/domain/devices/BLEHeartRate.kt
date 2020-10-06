package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.data.CarpDataTypes
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.common.serialization.NotSerializable
import dk.cachet.carp.common.toTrilean
import dk.cachet.carp.protocols.domain.sampling.SamplingConfiguration
import dk.cachet.carp.protocols.domain.sampling.carp.HearRateSamplingConfigurationBuilder
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * A generic BLE HR device.
 */
@Serializable
data class BLEHeartRate(
    override val roleName: String
) : DeviceDescriptor<BLEDeviceRegistration, BLEDeviceRegistrationBuilder>() {
    override val supportedDataTypes: Set<DataType> = setOf( CarpDataTypes.HEARTRATE )
    override val samplingConfiguration: Map<DataType, SamplingConfiguration> =
        mapOf(
            CarpDataTypes.HEARTRATE to HearRateSamplingConfigurationBuilder.build()
        )

    override fun createDeviceRegistrationBuilder(): BLEDeviceRegistrationBuilder =
        BLEDeviceRegistrationBuilder()

    override fun getRegistrationClass(): KClass<BLEDeviceRegistration> =
        BLEDeviceRegistration::class

    override fun isValidConfiguration(registration: BLEDeviceRegistration): Trilean =
        registration.deviceId.isNotBlank().toTrilean()
}

/**
 * A [DeviceRegistration] for [BLEHeartRate] specifying the vendor device ID, MAC address and name.
 */
@Serializable
data class BLEDeviceRegistration(
    override val deviceId: String,
    val macAddress: String,
    val deviceName: String
) : DeviceRegistration()

@Serializable( with = NotSerializable::class )
class BLEDeviceRegistrationBuilder : DeviceRegistrationBuilder<BLEDeviceRegistration> {
    var deviceId = ""
    var macAddress = "00:00:00:00:00:00"
    var deviceName = ""
    override fun build(): BLEDeviceRegistration =
        BLEDeviceRegistration( deviceId, macAddress, deviceName )
}
