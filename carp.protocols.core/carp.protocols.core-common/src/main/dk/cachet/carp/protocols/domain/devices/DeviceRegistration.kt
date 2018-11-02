package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.serialization.Serializable


/**
 * A [DeviceRegistration] configures a [DeviceDescriptor] as part of the deployment of a [StudyProtocol].
 */
@Serializable
open class DeviceRegistration(
    /**
     * An ID for the device, used to disambiguate between devices of the same type, as provided by the device itself.
     * It is up to specific types of devices to guarantee uniqueness across all devices of the same type.
     */
    var deviceId: String )
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( DeviceRegistration::class, "dk.cachet.carp.protocols.domain.devices.DeviceRegistration" ) }
    }
}