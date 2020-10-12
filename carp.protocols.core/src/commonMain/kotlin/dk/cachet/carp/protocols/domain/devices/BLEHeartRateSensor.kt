package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingSchemeList
import dk.cachet.carp.protocols.domain.sampling.SamplingConfiguration
import dk.cachet.carp.protocols.domain.sampling.carp.HeartRateSamplingScheme
import dk.cachet.carp.protocols.domain.tasks.measures.DataTypeMeasure
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * A Bluetooth Low Energy (BLE) device which implements a GATT Heart Rate service (https://www.bluetooth.com/specifications/gatt/services/).
 */
@Serializable
data class BLEHeartRateSensor( override val roleName: String ) :
    DeviceDescriptor<MACAddressDeviceRegistration, MACAddressDeviceRegistrationBuilder>()
{
    companion object
    {
        /**
         * Measure the number of heart contractions (beats) per minute
         */
        fun heartRate() = DataTypeMeasure( SamplingSchemes.HEART_RATE.type )
    }

    object SamplingSchemes : DataTypeSamplingSchemeList()
    {
        val HEART_RATE = add( HeartRateSamplingScheme ) // No configuration options available.
    }


    override val supportedDataTypes: Set<DataType> = emptySet()

    override val samplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): MACAddressDeviceRegistrationBuilder = MACAddressDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<MACAddressDeviceRegistration> = MACAddressDeviceRegistration::class
    override fun isValidConfiguration( registration: MACAddressDeviceRegistration ): Trilean = Trilean.TRUE
}
