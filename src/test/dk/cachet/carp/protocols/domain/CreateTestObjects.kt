package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.data.StubDataType
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
internal data class UnknownMasterDeviceDescriptor( override val roleName: String ) : MasterDeviceDescriptor()

@Serializable
internal data class UnknownDeviceDescriptor( override val roleName: String ) : DeviceDescriptor()

@Serializable
internal data class UnknownTaskDescriptor( override val name: String, override val measures: List<Measure> ) : TaskDescriptor()

@Serializable
internal data class UnknownTrigger( override val sourceDeviceRoleName: String ) : Trigger()

/**
 * Creates a study protocol which includes an unknown master device, unkown connected device, and unknown task triggered by an unknown trigger.
 */
fun serializeProtocolSnapshotIncludingUnknownTypes(): String
{
    val protocol = createComplexProtocol()
    val master = UnknownMasterDeviceDescriptor( "Unknown" )
    protocol.addMasterDevice( master )
    val connected = UnknownDeviceDescriptor( "Unknown 2" )
    protocol.addConnectedDevice( connected, master )
    val measures: List<Measure> = listOf( Measure( StubDataType( "Test" ) ), Measure( StubDataType( "Test 2" ) ) )
    val task = UnknownTaskDescriptor( "Unknown task", measures )
    val trigger = UnknownTrigger( master.roleName )
    protocol.addTriggeredTask( trigger, task, master )

    val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
    var serialized: String = snapshot.toJson()

    // Replace the strings which identify the types to load by the PolymorphicSerializer.
    // This will cause the types not to be found while deserializing, hence mimicking 'custom' types.
    serialized = serialized.replace( UnknownMasterDeviceDescriptor::class.qualifiedName!!, "com.unknown.CustomMasterDevice" )
    serialized = serialized.replace( UnknownDeviceDescriptor::class.qualifiedName!!, "com.unknown.CustomDevice" )
    serialized = serialized.replace( UnknownTaskDescriptor::class.qualifiedName!!, "com.unknown.CustomTask" )
    serialized = serialized.replace( UnknownTrigger::class.qualifiedName!!, "com.unknown.CustomTrigger" )

    return serialized
}

