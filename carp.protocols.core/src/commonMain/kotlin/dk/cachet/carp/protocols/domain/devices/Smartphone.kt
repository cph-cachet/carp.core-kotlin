package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.tasks.measures.*
import kotlinx.serialization.Serializable


/**
 * An internet-connected phone with built-in sensors.
 */
@Serializable
data class Smartphone( override val roleName: String ) : MasterDeviceDescriptor()
{
    companion object
    {
        /**
         * Factory to initialize sensor measures typically supported on smartphones.
         */
        val SENSOR_MEASURES: PhoneSensorMeasureFactory = PhoneSensorMeasure.Factory

        init
        {
            PolymorphicSerializer.registerSerializer(
                Smartphone::class,
                Smartphone.serializer(),
                "dk.cachet.carp.protocols.domain.devices.Smartphone" )
        }
    }

    override fun createRegistration(): DeviceRegistration = defaultDeviceRegistration()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}