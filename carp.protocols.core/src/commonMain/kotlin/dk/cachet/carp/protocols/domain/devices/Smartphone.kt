package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.protocols.domain.tasks.measures.*
import kotlinx.serialization.Serializable


/**
 * An internet-connected phone with built-in sensors.
 */
@Serializable
data class Smartphone( override val roleName: String ) : MasterDeviceDescriptor<DefaultDeviceRegistrationBuilder>()
{
    companion object
    {
        /**
         * Factory to initialize sensor measures typically supported on smartphones.
         */
        val SENSOR_MEASURES: PhoneSensorMeasureFactory = PhoneSensorMeasure.Factory
    }

    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}