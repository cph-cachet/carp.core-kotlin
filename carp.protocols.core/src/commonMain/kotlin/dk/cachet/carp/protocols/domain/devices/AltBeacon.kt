package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.common.toTrilean
import kotlinx.serialization.Serializable


/**
 * A beacon meeting the open AltBeacon standard.
 */
@Serializable
data class AltBeacon( override val roleName: String ) : DeviceDescriptor()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                AltBeacon::class,
                AltBeacon.serializer(),
                "dk.cachet.carp.protocols.domain.devices.AltBeacon" )
        }
    }

    override fun createRegistration(): AltBeaconDeviceRegistration = AltBeaconDeviceRegistration.createUnconfigured()
    override fun isValidConfiguration( registration: DeviceRegistration ): Trilean
        = ( registration is AltBeaconDeviceRegistration ).toTrilean()
}