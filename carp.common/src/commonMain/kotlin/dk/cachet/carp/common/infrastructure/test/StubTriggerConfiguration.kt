package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.data.NoData
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.triggers.TriggerConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class StubTriggerConfiguration(
    override val sourceDeviceRoleName: String,
    val uniqueProperty: String = "Unique",
    @Transient
    override val requiresPrimaryDevice: Boolean = false
) : TriggerConfiguration<NoData>()
{
    constructor( device: AnyDeviceConfiguration ) : this( device, "Unique" )
    constructor( device: AnyDeviceConfiguration, uniqueName: String ) : this( device.roleName, uniqueName )
}
