@file:Suppress( "WildcardImport" )

package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.*
import dk.cachet.carp.common.application.tasks.*
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.time.Duration


typealias SmartphoneDeviceRegistration = DefaultDeviceRegistration
typealias SmartphoneDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder


/**
 * An internet-connected phone with built-in sensors.
 */
@Serializable
data class Smartphone(
    override val roleName: String,
    override val isOptional: Boolean = false,
    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()
) : PrimaryDeviceConfiguration<SmartphoneDeviceRegistration, SmartphoneDeviceRegistrationBuilder>()
{
    constructor( roleName: String, isOptional: Boolean = false, builder: SmartphoneBuilder.() -> Unit ) :
        this( roleName, isOptional, SmartphoneBuilder().apply( builder ).buildSamplingConfiguration() )

    /**
     * All the sensors commonly available on smartphones.
     */
    @Suppress( "MagicNumber" )
    object Sensors : DataTypeSamplingSchemeMap()
    {
        /**
         *  Geographic location data, representing latitude and longitude within the World Geodetic System 1984.
         */
        val GEOLOCATION = add( AdaptiveGranularitySamplingScheme( CarpDataTypes.GEOLOCATION ) )

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
        val STEP_COUNT = add( NoOptionsSamplingScheme( CarpDataTypes.STEP_COUNT ) ) // No configuration options available.

        /**
         * Acceleration along perpendicular x, y, and z axes,
         * as measured by the phone's accelerometer and calibrated to exclude gravity and sensor bias.
         * This uses the same coordinate system as the other sensors referring to x, y, and z axes.
         *
         * Android (https://developer.android.com/guide/topics/sensors/sensors_overview):
         * - This corresponds to `TYPE_LINEAR_ACCELERATION`.
         * - The sampling scheme's default measure interval corresponds to `SENSOR_DELAY_NORMAL` (200 ms).
         * - The linear acceleration sensor always has an offset, which you need to remove: https://developer.android.com/guide/topics/sensors/sensors_motion
         * - Only available starting from Android 3.0 (API Level 11): earlier versions don't support setting the interval as an absolute value.
         */
        val NON_GRAVITATIONAL_ACCELERATION = add(
            IntervalSamplingScheme( CarpDataTypes.NON_GRAVITATIONAL_ACCELERATION, Duration.milliseconds( 200 ) )
        )

        /**
         * Rate of rotation around perpendicular x, y and z axes,
         * as measured by the phone's gyroscope and calibrated to remove drift and noise.
         * This uses the same coordinate system as the other sensors referring to x, y, and z axes.
         *
         * Android (https://developer.android.com/guide/topics/sensors/sensors_overview):
         * - This corresponds to `TYPE_GYROSCOPE`.
         * - The sampling scheme's default measure interval corresponds to `SENSOR_DELAY_NORMAL` (200 ms).
         * - Only available starting from Android 3.0 (API Level 11): earlier versions don't support setting the interval as an absolute value.
         */
        val ANGULAR_VELOCITY = add(
            IntervalSamplingScheme( CarpDataTypes.ANGULAR_VELOCITY, Duration.milliseconds( 200 ) )
        )
    }

    /**
     * ALl tasks commonly available on smartphones.
     */
    object Tasks : TaskConfigurationList()
    {
        /**
         * Redirect to a web page which contains the task which needs to be performed.
         * The passive measures are started when the website is opened and stopped when it is closed.
         */
        val WEB = add { WebTaskBuilder() }
    }

    override fun getSupportedDataTypes(): Set<DataType> = Sensors.keys
    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap = Sensors

    override fun createDeviceRegistrationBuilder(): SmartphoneDeviceRegistrationBuilder = SmartphoneDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<SmartphoneDeviceRegistration> = SmartphoneDeviceRegistration::class
    override fun isValidRegistration( registration: SmartphoneDeviceRegistration ) = Trilean.TRUE
}


/**
 * A helper class to configure and construct immutable [Smartphone] classes.
 */
class SmartphoneBuilder : DeviceConfigurationBuilder<SmartphoneSamplingConfigurationMapBuilder>()
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
     * Configure sampling configuration for [CarpDataTypes.GEOLOCATION].
     */
    fun geolocation( builder: AdaptiveGranularitySamplingConfigurationBuilder.() -> Unit ): SamplingConfiguration =
        addConfiguration( Smartphone.Sensors.GEOLOCATION, builder )
}
