package dk.cachet.carp.protocols.domain.serialization

import dk.cachet.carp.protocols.domain.devices.CustomMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor


/**
 * Serializer for [MasterDeviceDescriptor] which wraps unknown extending types using [CustomMasterDeviceDescriptor].
 */
object MasterDeviceDescriptorSerializer : UnknownPolymorphicSerializer<MasterDeviceDescriptor, CustomMasterDeviceDescriptor>( CustomMasterDeviceDescriptor::class )
{
    override fun createWrapper( className: String, json: String ): CustomMasterDeviceDescriptor
    {
        return CustomMasterDeviceDescriptor( className, json )
    }
}