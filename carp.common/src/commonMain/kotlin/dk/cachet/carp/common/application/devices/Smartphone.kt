package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.TimeSpan
import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.Smartphone.SensorsSamplingSchemes.GEOLOCATION
import dk.cachet.carp.common.application.sampling.DataTypeSamplingScheme
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeList
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.sampling.SamplingConfigurationMapBuilder
import dk.cachet.carp.common.application.sampling.carp.GeolocationSamplingConfigurationBuilder
import dk.cachet.carp.common.application.sampling.carp.GeolocationSamplingScheme
import dk.cachet.carp.common.application.sampling.carp.StepCountSamplingScheme
import dk.cachet.carp.common.application.tasks.measures.PassiveMeasure
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


typealias SmartphoneDeviceRegistration = DefaultDeviceRegistration
typealias SmartphoneDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder


/**
 * An internet-connected phone with built-in sensors.
 */
@Serializable
data class Smartphone(
    override val roleName: String,
    override val samplingConfiguration: Map<DataType, SamplingConfiguration>
) : MasterDeviceDescriptor<SmartphoneDeviceRegistration, SmartphoneDeviceRegistrationBuilder>()
{
    constructor( roleName: String, builder: SmartphoneBuilder.() -> Unit = { } ) :
        this( roleName, SmartphoneBuilder().apply( builder ).buildSamplingConfiguration() )

    /**
     * All the data types and sampling schemes of sensors commonly available on smartphones.
     */
    object SensorsSamplingSchemes : DataTypeSamplingSchemeList()
    {
        val GEOLOCATION = add( GeolocationSamplingScheme( TimeSpan.fromMinutes( 1.0 ) ) )

        /**
         * Steps within recorded time intervals as reported by a phone's dedicated hardware sensor.
         * Data rate is determined by the sensor.
         *
         * Android (https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-stepcounter):
         * - There is a latency of up to 10 s.
         * - Only available starting from Android 4.4.
         *
         * TODO: Android can also 'listen' for steps, which has a delay of about 2 s but is less accurate.
         *       Each 'step' is reported as an event, so this would map to a different DataType (e.g. `Step`).
         *       Not certain this is available on iPhone.
         */
        val STEP_COUNT = add( StepCountSamplingScheme ) // No configuration options available.
    }

    /**
     * A factory to create measures for sensors commonly available on smartphones.
     */
    object Sensors
    {
        private fun <T : DataTypeSamplingScheme<*>> measureOf( samplingScheme: T, duration: TimeSpan ) =
            PassiveMeasure( samplingScheme.type, duration )

        /**
         * Measure geographic location data (longitude and latitude).
         */
        fun geolocation( duration: TimeSpan = TimeSpan.INFINITE ) = measureOf( SensorsSamplingSchemes.GEOLOCATION, duration )

        /**
         * Measure number of steps a participant has taken in a recorded time interval.
         */
        fun stepCount( duration: TimeSpan = TimeSpan.INFINITE ) = measureOf( SensorsSamplingSchemes.STEP_COUNT, duration )
    }

    override val supportedDataTypes: Set<DataType> = SensorsSamplingSchemes.map { it.type }.toSet()

    override fun createDeviceRegistrationBuilder(): SmartphoneDeviceRegistrationBuilder = SmartphoneDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<SmartphoneDeviceRegistration> = SmartphoneDeviceRegistration::class
    override fun isValidConfiguration( registration: SmartphoneDeviceRegistration ) = Trilean.TRUE
}


/**
 * A helper class to configure and construct immutable [Smartphone] classes.
 */
class SmartphoneBuilder : DeviceDescriptorBuilder<SmartphoneSamplingConfigurationMapBuilder>()
{
    override fun createSamplingConfigurationMapBuilder(): SmartphoneSamplingConfigurationMapBuilder =
        SmartphoneSamplingConfigurationMapBuilder()
}


/**
 * A helper class to construct sampling configurations for a [Smartphone].
 */
class SmartphoneSamplingConfigurationMapBuilder : SamplingConfigurationMapBuilder()
{
    /**
     * Configure sampling configuration for [GeolocationSamplingScheme].
     */
    fun geolocation( builder: GeolocationSamplingConfigurationBuilder.() -> Unit ): SamplingConfiguration =
        addConfiguration( GEOLOCATION, builder )
}
