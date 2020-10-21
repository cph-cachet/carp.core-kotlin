package dk.cachet.carp.client.domain

import dk.cachet.carp.common.ImplementAsDataClass
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Describes the status of a [device] in a study runtime.
 */
@ImplementAsDataClass
sealed class DeviceRegistrationStatus
{
    abstract val device: AnyDeviceDescriptor


    data class Unregistered internal constructor( override val device: AnyDeviceDescriptor ) :
        DeviceRegistrationStatus()

    data class Registered internal constructor(
        override val device: AnyDeviceDescriptor,
        val registration: DeviceRegistration
    ) : DeviceRegistrationStatus()
}
