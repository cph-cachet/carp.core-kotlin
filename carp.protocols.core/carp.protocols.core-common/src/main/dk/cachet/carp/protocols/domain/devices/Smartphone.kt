package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * An internet-connected phone with built-in sensors.
 */
@Serializable
data class Smartphone( override val roleName: String ) : MasterDeviceDescriptor()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( Smartphone::class, "dk.cachet.carp.protocols.domain.devices.Smartphone" ) }
    }

    override fun createRegistration(): DeviceRegistration = defaultDeviceRegistration()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}