package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.common.serialization.*
import dk.cachet.carp.protocols.domain.triggers.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.tasks.measures.*
import kotlinx.serialization.Serializable


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers).
 */
fun createEmptyProtocol(): StudyProtocol
{
    val alwaysSameOwner = ProtocolOwner( UUID( "27879e75-ccc1-4866-9ab3-4ece1b735052" ) )
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
    val measures = listOf( StubMeasure() )
    val task = StubTaskDescriptor( "Task", measures )
    with ( protocol )
    {
        addMasterDevice( masterDevice )
        addConnectedDevice( connectedDevice, masterDevice )
        addConnectedDevice( chainedMasterDevice, masterDevice )
        addConnectedDevice( chainedConnectedDevice, chainedMasterDevice )
        addTriggeredTask( trigger, task, masterDevice )
    }

    return protocol
}

@Serializable
internal data class UnknownMasterDeviceDescriptor( override val roleName: String ) : MasterDeviceDescriptor()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownMasterDeviceDescriptor::class,
                UnknownMasterDeviceDescriptor.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownMasterDeviceDescriptor" )
        }
    }

    override fun createRegistration(): DeviceRegistration = defaultDeviceRegistration()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable
internal data class UnknownDeviceDescriptor( override val roleName: String ) : DeviceDescriptor()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownDeviceDescriptor::class,
                UnknownDeviceDescriptor.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownDeviceDescriptor" )
        }
    }

    override fun createRegistration(): DeviceRegistration = defaultDeviceRegistration()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable
internal class UnknownDeviceRegistration( override var deviceId: String ) : DeviceRegistration()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownDeviceRegistration::class,
                UnknownDeviceRegistration.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownDeviceRegistration" )
        }
    }
}

@Serializable
internal data class UnknownTaskDescriptor(
    override val name: String,
    @Serializable( PolymorphicArrayListSerializer::class )
    override val measures: List<Measure> ) : TaskDescriptor()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownTaskDescriptor::class,
                UnknownTaskDescriptor.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownTaskDescriptor" )
        }
    }
}

@Serializable
internal data class UnknownMeasure( override val type: DataType ) : Measure()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownMeasure::class,
                UnknownMeasure.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownMeasure" )
        }
    }
}

@Serializable
internal data class UnknownTrigger( override val sourceDeviceRoleName: String ) : Trigger()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownTrigger::class,
                UnknownTrigger.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownTrigger" )
        }
    }
}

/**
 * Creates a study protocol which includes:
 * (1) an unknown master device and unknown connected device
 * (2) unknown task with an unknown measure and unknown data type, triggered by an unknown trigger
 * (3) known task with an unknown measure and known data type
 * There is thus exactly one unknown object for each of these types, except for 'Measure' which has two.
 */
fun serializeProtocolSnapshotIncludingUnknownTypes(): String
{
    val protocol = createComplexProtocol()

    // (1) Add unknown master with unknown connected device.
    val master = UnknownMasterDeviceDescriptor( "Unknown" )
    protocol.addMasterDevice( master )
    val connected = UnknownDeviceDescriptor( "Unknown 2" )
    protocol.addConnectedDevice( connected, master )

    // (2) Add unknown task with unknown measure.
    val measures: List<Measure> = listOf( UnknownMeasure( STUB_DATA_TYPE ), StubMeasure( STUB_DATA_TYPE ) )
    val task = UnknownTaskDescriptor( "Unknown task", measures )
    val trigger = UnknownTrigger( master.roleName )
    protocol.addTriggeredTask( trigger, task, master )

    // (3) Add known task with unknown measure.
    val task2 = StubTaskDescriptor( "Known task", listOf( UnknownMeasure( STUB_DATA_TYPE ) ) )
    protocol.addTriggeredTask( trigger, task2, master )

    val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
    var serialized: String = snapshot.toJson()

    // Replace the strings which identify the types to load by the PolymorphicSerializer.
    // This will cause the types not to be found while deserializing, hence mimicking 'custom' types.
    serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownMasterDeviceDescriptor", "com.unknown.CustomMasterDevice" )
    serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownDeviceDescriptor", "com.unknown.CustomDevice" )
    serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownTaskDescriptor", "com.unknown.CustomTask" )
    serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownMeasure", "com.unknown.CustomMeasure" )
    serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownTrigger", "com.unknown.CustomTrigger" )

    return serialized
}

