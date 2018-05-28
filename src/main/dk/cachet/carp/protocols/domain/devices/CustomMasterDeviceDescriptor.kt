package dk.cachet.carp.protocols.domain.devices

import kotlinx.serialization.Serializable


/**
 * A wrapper used to load extending types from [MasterDeviceDescriptor]s serialized as JSON which are unknown at runtime.
 */
@Serializable
data class CustomMasterDeviceDescriptor( val jsonSource: String ) : MasterDeviceDescriptor()
{
    override val roleName: String

    init
    {
        // TODO: Parse JSON to get common properties.
        roleName = "Custom"
    }
}