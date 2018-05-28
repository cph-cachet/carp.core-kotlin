package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.triggers.*
import dk.cachet.carp.protocols.domain.tasks.*
import kotlinx.serialization.Serializable
import java.util.*


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers).
 */
fun createEmptyProtocol(): StudyProtocol
{
    val alwaysSameOwner = ProtocolOwner( UUID.fromString( "27879e75-ccc1-4866-9ab3-4ece1b735052" ) )
    return StudyProtocol( alwaysSameOwner, "Test protocol" )
}

/**
 * Creates a study protocol with a couple of devices and tasks added.
 */
fun createComplexProtocol(): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val masterDevice = StubMasterDeviceDescriptor()
    val connectedDevice = StubDeviceDescriptor()
    val chainedMasterDevice = StubMasterDeviceDescriptor( "Chained master" )
    val chainedConnectedDevice = StubDeviceDescriptor( "Chained connected" )
    val trigger = StubTrigger( connectedDevice )
    with ( protocol )
    {
        addMasterDevice( masterDevice )
        addConnectedDevice( connectedDevice, masterDevice )
        addConnectedDevice( chainedMasterDevice, masterDevice )
        addConnectedDevice( chainedConnectedDevice, chainedMasterDevice )
        addTriggeredTask( trigger, StubTaskDescriptor(), masterDevice )
    }

    return protocol
}

@Serializable
internal data class UnknownMasterDeviceDescriptor( override val roleName: String = "Unknown" ) : MasterDeviceDescriptor()

/**
 * Creates a study protocol which includes an [UnknownMasterDeviceDescriptor].
 * TODO: Include unknown connectedDevices, tasks, and triggers.
 */
fun serializeProtocolSnapshotIncludingUnknownTypes(): String
{
    val protocol = createComplexProtocol()
    val master = UnknownMasterDeviceDescriptor()
    protocol.addMasterDevice( master )

    val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
    var serialized: String = snapshot.toJson()

    // Replace the strings which identify the types to load by the PolymorphSerializer.
    // This will cause the types not to be found while deserializing, hence mimicking 'custom' types.
    serialized = serialized.replace( UnknownMasterDeviceDescriptor::class.qualifiedName!!, "com.unknown.CustomMasterDevice" )

    return serialized
}

