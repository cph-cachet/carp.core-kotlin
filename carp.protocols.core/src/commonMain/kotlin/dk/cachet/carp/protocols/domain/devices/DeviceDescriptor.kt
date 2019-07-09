package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.*
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import kotlinx.serialization.Serializable


/**
 * Describes any type of electronic device, such as a sensor, video camera, desktop computer, or smartphone
 * that collects data which can be incorporated into the platform after it has been processed by a master device (potentially itself).
 * Optionally, a device can present output and receive user input.
 *
 * TODO: Does this also allow specifying dynamic devices? E.g., 'closest smartphone'. Perhaps a 'DeviceSelector'?
 */
@Serializable
abstract class DeviceDescriptor<out T: DeviceRegistrationBuilder> : Immutable( notImmutableErrorFor( DeviceDescriptor::class ) )
{
    /**
     * A name which describes how the device participates within the study protocol; it's 'role'.
     * E.g., "Patient's phone"
     */
    abstract val roleName: String

    abstract fun createDeviceRegistrationBuilder(): T

    /**
     * Create a [DeviceRegistration] which can be used to configure this device for deployment.
     * Use [builder] to configure device-specific registration options, if any.
     */
    fun createRegistration( builder: T.() -> Unit = {} ): DeviceRegistration
        = createDeviceRegistrationBuilder().apply( builder ).build()

    /**
     * Determines whether the given [registration] is configured correctly for this type of device.
     * Devices rely on a concrete [DeviceRegistration] to determine the specific configuration needed for them.
     */
    abstract fun isValidConfiguration( registration: DeviceRegistration ): Trilean
}