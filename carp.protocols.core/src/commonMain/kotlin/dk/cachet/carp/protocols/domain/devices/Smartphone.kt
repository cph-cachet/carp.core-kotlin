package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.SamplingConfiguration
import dk.cachet.carp.protocols.domain.tasks.measures.PhoneSensorMeasure
import dk.cachet.carp.protocols.domain.tasks.measures.PhoneSensorSamplingConfigurationMapBuilder
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * An internet-connected phone with built-in sensors.
 */
@Serializable
data class Smartphone(
    override val roleName: String,
    override val samplingConfiguration: Map<DataType, SamplingConfiguration>
) : MasterDeviceDescriptor<DefaultDeviceRegistration, DefaultDeviceRegistrationBuilder>()
{
    constructor( roleName: String, builder: SmartphoneBuilder.() -> Unit = { } ) :
        this( roleName, SmartphoneBuilder().apply( builder ).buildSamplingConfiguration() )

    companion object
    {
        /**
         * A factory to create measures for sensors commonly available on smartphones.
         */
        val Sensors: PhoneSensorMeasure.Factory = PhoneSensorMeasure.Factory

        /**
         * All the data types and sampling schemes of sensor commonly available on smartphones.
         */
        val SensorsSamplingSchemes = PhoneSensorMeasure.SamplingSchemes
    }

    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DefaultDeviceRegistration> = DefaultDeviceRegistration::class
    override fun isValidConfiguration( registration: DefaultDeviceRegistration ) = Trilean.TRUE
}


/**
 * A helper class to configure and construct immutable [Smartphone] classes.
 *
 * TODO: Once `Smartphone` supports additional measures (e.g., surveys), we will need to aggregate multiple builders.
 */
class SmartphoneBuilder : DeviceDescriptorBuilder<PhoneSensorSamplingConfigurationMapBuilder>()
{
    override fun createSamplingConfigurationMapBuilder(): PhoneSensorSamplingConfigurationMapBuilder =
        PhoneSensorSamplingConfigurationMapBuilder()
}
