package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * A concrete [DeviceRegistration] which solely implements the base properties and nothing else.
 *
 * The base class can't be made concrete since this would prevent being able to serialize extending types (constructors may not contain parameters which are not properties).
 */
@Serializable
class DefaultDeviceRegistration( override var deviceId: String ) : DeviceRegistration()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( DefaultDeviceRegistration::class, "dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration" ) }
    }
}