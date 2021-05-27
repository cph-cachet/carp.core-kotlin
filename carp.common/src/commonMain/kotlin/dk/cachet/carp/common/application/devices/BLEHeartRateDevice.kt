package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeList
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingScheme
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskDescriptorList
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * A Bluetooth Low Energy (BLE) device which implements a GATT Heart Rate service (https://www.bluetooth.com/specifications/gatt/services/).
 */
@Serializable
data class BLEHeartRateDevice(
    override val roleName: String
) : DeviceDescriptor<MACAddressDeviceRegistration, MACAddressDeviceRegistrationBuilder>()
{
    object Sensors : DataTypeSamplingSchemeList()
    {
        /**
         * The number of heart contractions (beats) per minute (bpm).
         */
        val HEART_RATE = add( NoOptionsSamplingScheme( CarpDataTypes.HEART_RATE ) )

        /**
         * The time interval between two consecutive heartbeats (R-R interval).
         */
        val RR_INTERVAL = add( NoOptionsSamplingScheme( CarpDataTypes.RR_INTERVAL ) )

        /**
         * Whether or not the device is making proper contact.
         */
        val SENSOR_SKIN_CONTACT = add( NoOptionsSamplingScheme( CarpDataTypes.SENSOR_SKIN_CONTACT ) )
    }

    object Tasks : TaskDescriptorList()


    override fun getSupportedDataTypes(): Set<DataType> = Sensors.getDataTypes()
    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): MACAddressDeviceRegistrationBuilder = MACAddressDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<MACAddressDeviceRegistration> = MACAddressDeviceRegistration::class
    override fun isValidRegistration( registration: MACAddressDeviceRegistration ): Trilean = Trilean.TRUE
}
