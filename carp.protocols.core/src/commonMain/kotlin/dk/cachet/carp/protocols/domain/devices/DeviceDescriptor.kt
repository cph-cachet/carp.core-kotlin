package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.*
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import kotlinx.serialization.*


/**
 * Describes any type of electronic device, such as a sensor, video camera, desktop computer, or smartphone
 * that collects data which can be incorporated into the platform after it has been processed by a master device (potentially itself).
 * Optionally, a device can present output and receive user input.
 *
 * TODO: Does this also allow specifying dynamic devices? E.g., 'closest smartphone'. Perhaps a 'DeviceSelector'?
 */
@Serializable
abstract class DeviceDescriptor : Immutable( notImmutableErrorFor( DeviceDescriptor::class ) )
{
    /**
     * A name which describes how the device participates within the study protocol; it's 'role'.
     * E.g., "Patient's phone"
     */
    @Transient
    abstract val roleName: String

    /**
     * Create a suitable [DeviceRegistration] which can be used to configure this device for deployment.
     * Default registration for devices typically only involves assigning a unique ID to them.
     * However, in case more specific configuration is needed as part of registration, [DeviceRegistration] can be extended.
     */
    abstract fun createRegistration(): DeviceRegistration

    /**
     * Determines whether the given [registration] is configured correctly for this type of device.
     * Specific devices may extend from [DeviceRegistration] in case custom configuration is needed for them.
     */
    abstract fun isValidConfiguration( registration: DeviceRegistration ): Trilean
}