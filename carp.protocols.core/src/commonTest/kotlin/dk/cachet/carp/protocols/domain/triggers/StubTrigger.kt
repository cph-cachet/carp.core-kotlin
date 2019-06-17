package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.*


@Serializable
data class StubTrigger(
    override val sourceDeviceRoleName: String,
    val uniqueProperty: String = "Unique",
    @Transient
    override val requiresMasterDevice: Boolean = false ) : Trigger()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                StubTrigger::class,
                StubTrigger.serializer(),
                "dk.cachet.carp.protocols.domain.triggers.StubTrigger" )
        }
    }

    constructor( device: DeviceDescriptor ) : this( device, "Unique" )
    constructor( device: DeviceDescriptor, uniqueName: String ) : this( device.roleName, uniqueName )
}


