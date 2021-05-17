package dk.cachet.carp.clients.domain

import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration


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
