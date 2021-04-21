package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeList
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.sampling.carp.HeartRateSamplingScheme
import dk.cachet.carp.common.application.sampling.carp.RRIntervalSamplingScheme
import dk.cachet.carp.common.application.sampling.carp.SensorSkinContactSamplingScheme
import dk.cachet.carp.common.application.tasks.measures.DataTypeMeasure
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * A Bluetooth Low Energy (BLE) device which implements a GATT Heart Rate service (https://www.bluetooth.com/specifications/gatt/services/).
 */
@Serializable
data class BLEHeartRateSensor(
    override val roleName: String
) : DeviceDescriptor<MACAddressDeviceRegistration, MACAddressDeviceRegistrationBuilder>()
{
    companion object
    {
        /**
         * Measure the number of heart contractions (beats) per minute (bpm).
         */
        fun heartRate() = DataTypeMeasure( SamplingSchemes.HEART_RATE.type )

        /**
         * Measure the time interval between consecutive heartbeats (R-R intervals).
         */
        fun rrInterval() = DataTypeMeasure( SamplingSchemes.RR_INTERVAL.type )

        /**
         * Measure whether the sensor is making proper skin contact.
         */
        fun sensorSkinContact() = DataTypeMeasure( SamplingSchemes.SENSOR_SKIN_CONTACT.type )
    }

    object SamplingSchemes : DataTypeSamplingSchemeList()
    {
        val HEART_RATE = add( HeartRateSamplingScheme ) // No configuration options available.
        val RR_INTERVAL = add( RRIntervalSamplingScheme )
        val SENSOR_SKIN_CONTACT = add( SensorSkinContactSamplingScheme )
    }


    override val supportedDataTypes: Set<DataType> = SamplingSchemes.map { it.type }.toSet()
    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): MACAddressDeviceRegistrationBuilder = MACAddressDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<MACAddressDeviceRegistration> = MACAddressDeviceRegistration::class
    override fun isValidConfiguration( registration: MACAddressDeviceRegistration ): Trilean = Trilean.TRUE
}
