package dk.cachet.carp.clients.domain

import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceRegistration


/**
 * Describes the status of a [device] in a study runtime.
 */
@ImplementAsDataClass
sealed class DeviceRegistrationStatus
{
    abstract val device: AnyDeviceConfiguration


    data class Unregistered internal constructor( override val device: AnyDeviceConfiguration ) :
        DeviceRegistrationStatus()

    data class Registered internal constructor(
        override val device: AnyDeviceConfiguration,
        val registration: DeviceRegistration
    ) : DeviceRegistrationStatus()
}
