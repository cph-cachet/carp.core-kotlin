package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers).
 */
fun createEmptyProtocol(): StudyProtocol
{
    val alwaysSameOwner = ProtocolOwner( UUID( "f3f4d91b-56b5-4117-bb98-7e2923cb2223" ) )
    return StudyProtocol( alwaysSameOwner, "Test protocol" )
}

/**
 * Creates a study protocol with a single master device which has a single connected device.
 */
fun createSingleMasterWithConnectedDeviceProtocol(
    masterDeviceName: String = "Master",
    connectedDeviceName: String = "Connected" ): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val master = StubMasterDeviceDescriptor( masterDeviceName )
    protocol.addMasterDevice( master )
    protocol.addConnectedDevice( StubMasterDeviceDescriptor( connectedDeviceName ), master )
    return protocol
}

@Serializable
internal data class UnknownMasterDeviceDescriptor( override val roleName: String ) : MasterDeviceDescriptor()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( UnknownMasterDeviceDescriptor::class, "dk.cachet.carp.deployment.domain.UnknownMasterDeviceDescriptor" ) }
    }

    override fun isValidConfiguration( configuration: DeviceConfiguration ) = Trilean.TRUE
}