package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.protocols.domain.tasks.measures.*
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * An internet-connected phone with built-in sensors.
 */
@Serializable
data class Smartphone( override val roleName: String )
    : MasterDeviceDescriptor<DefaultDeviceRegistration, DefaultDeviceRegistrationBuilder>(), PhoneSensorMeasureFactory by PhoneSensorMeasure.Factory
{
    companion object : PhoneSensorMeasureFactory by PhoneSensorMeasure.Factory

    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DefaultDeviceRegistration> = DefaultDeviceRegistration::class
    override fun isValidConfiguration( registration: DefaultDeviceRegistration ) = Trilean.TRUE
}
