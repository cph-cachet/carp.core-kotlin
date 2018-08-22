package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.serialization.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.triggers.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.ReferenceArraySerializer
import kotlinx.serialization.json.*


// Custom serializers for StudyProtocolSnapshot which enable deserializing types that are unknown at runtime, yet extend from a known base type.
private object MasterDevicesSerializer : KSerializer<Array<MasterDeviceDescriptor>> by ReferenceArraySerializer<MasterDeviceDescriptor, MasterDeviceDescriptor>(
    MasterDeviceDescriptor::class,
    createUnknownPolymorphicSerializer { className, json -> CustomMasterDeviceDescriptor( className, json ) }
)
private object DevicesSerializer : KSerializer<Array<DeviceDescriptor>> by ReferenceArraySerializer(
    DeviceDescriptor::class,
    DeviceDescriptorSerializer
)
private object DeviceDescriptorSerializer : UnknownPolymorphicSerializer<DeviceDescriptor, DeviceDescriptor>( DeviceDescriptor::class, false )
{
    override fun createWrapper( className: String, json: String ): DeviceDescriptor
    {
        val parser = JsonTreeParser( json )
        val jsonObject = parser.readFully() as JsonObject
        val isMasterDevice = jsonObject.containsKey( MasterDeviceDescriptor::isMasterDevice.name )
        return if ( isMasterDevice )
            CustomMasterDeviceDescriptor( className, json )
            else CustomDeviceDescriptor( className, json )
    }
}
private object TasksSerializer : KSerializer<Array<TaskDescriptor>> by ReferenceArraySerializer<TaskDescriptor, TaskDescriptor>(
    TaskDescriptor::class,
    createUnknownPolymorphicSerializer { className, json -> CustomTaskDescriptor( className, json ) }
)
private object TriggerSerializer : UnknownPolymorphicSerializer<Trigger, CustomTrigger>( CustomTrigger::class )
{
    override fun createWrapper( className: String, json: String): CustomTrigger
    {
        return CustomTrigger( className, json )
    }
}


/**
 * A serializable snapshot of a [StudyProtocol] at the moment in time when it was created.
 */
@Serializable
data class StudyProtocolSnapshot(
    val ownerId: String,
    val name: String,
    @SerializableWith( MasterDevicesSerializer::class )
    val masterDevices: Array<MasterDeviceDescriptor>,
    @SerializableWith( DevicesSerializer::class )
    val connectedDevices: Array<DeviceDescriptor>,
    val connections: Array<DeviceConnection>,
    @SerializableWith( TasksSerializer::class )
    val tasks: Array<TaskDescriptor>,
    val triggers: Array<TriggerWithId>,
    val triggeredTasks: Array<TriggeredTask> )
{
    @Serializable
    data class DeviceConnection( val roleName: String, val connectedToRoleName: String )

    @Serializable
    data class TriggerWithId(
        val id: Int,
        @SerializableWith( TriggerSerializer::class )
        val trigger: Trigger )

    @Serializable
    data class TriggeredTask( val triggerId: Int, val taskName: String, val targetDeviceRoleName: String )

    companion object Factory
    {
        /**
         * Create a snapshot of the specified [StudyProtocol].
         *
         * @param protocol The [StudyProtocol] to create a snapshot for.
         */
        fun fromProtocol( protocol: StudyProtocol ): StudyProtocolSnapshot
        {
            // Uniquely identify each trigger.
            var curTriggerId = 0
            val triggers = protocol.triggers.map { TriggerWithId( curTriggerId++, it ) }.toTypedArray()

            return StudyProtocolSnapshot(
                ownerId = protocol.owner.id.toString(),
                name = protocol.name,
                masterDevices = protocol.masterDevices.toTypedArray(),
                connectedDevices = protocol.devices.minus( protocol.masterDevices ).toTypedArray(),
                connections = protocol.masterDevices.flatMap { getConnections( protocol, it ) }.toTypedArray(),
                tasks = protocol.tasks.toTypedArray(),
                triggers = triggers,
                triggeredTasks = triggers
                    .flatMap { idTrigger -> protocol.getTriggeredTasks( idTrigger.trigger ).map { Pair( idTrigger, it ) } }
                    .map { (idTrigger, taskInfo) -> TriggeredTask( idTrigger.id, taskInfo.task.name, taskInfo.device.roleName ) }.toTypedArray()
            )
        }

        /**
         * Create a snapshot from JSON serialized using the built-in serializer.
         *
         * @param json The JSON which was serialized using the built-in serializer (`StudyProtocolSnapshot.toJson`).
         */
        fun fromJson( json: String ): StudyProtocolSnapshot
        {
            return JSON.parse( json )
        }

        private fun getConnections( protocol: StudyProtocol, masterDevice: MasterDeviceDescriptor ): Iterable<DeviceConnection>
        {
            val connections: MutableList<DeviceConnection> = mutableListOf()

            protocol.getConnectedDevices( masterDevice ).forEach {
                connections.add( DeviceConnection( it.roleName, masterDevice.roleName ) )
                if ( it is MasterDeviceDescriptor )
                {
                    connections.addAll( getConnections( protocol, it ) )
                }
            }

            return connections
        }
    }


    /**
     * Serialize to JSON using the built-in serializer.
     */
    fun toJson(): String
    {
        return JSON.stringify( this )
    }


    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( other !is StudyProtocolSnapshot ) return false

        if ( ownerId != other.ownerId ) return false
        if ( name != other.name ) return false

        if ( !arrayEquals( masterDevices, other.masterDevices ) ) return false
        if ( !arrayEquals( connectedDevices, other.connectedDevices ) ) return false
        if ( !arrayEquals( connections, other.connections ) ) return false
        if ( !arrayEquals( tasks, other.tasks ) ) return false
        if ( !arrayEquals( triggers, other.triggers ) ) return false
        if ( !arrayEquals( triggeredTasks, other.triggeredTasks ) ) return false

        return true
    }

    private fun <T> arrayEquals( a1: Array<T>, a2: Array<T> ): Boolean
    {
        val count = a1.count()
        return count == a2.count() && a1.intersect( a2.toList() ).count() == count
    }

    override fun hashCode(): Int
    {
        var result = ownerId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + masterDevices.sortedWith( compareBy { it.roleName } ).toTypedArray().contentDeepHashCode()
        result = 31 * result + connectedDevices.sortedWith( compareBy { it.roleName } ).toTypedArray().contentDeepHashCode()
        result = 31 * result + connections.sortedWith( compareBy( { it.roleName }, { it.connectedToRoleName } ) ).toTypedArray().contentDeepHashCode()
        result = 31 * result + tasks.sortedWith( compareBy { it.name } ).toTypedArray().contentDeepHashCode()
        result = 31 * result + triggers.sortedWith( compareBy { it.id } ).toTypedArray().contentDeepHashCode()
        result = 31 * result + triggeredTasks.sortedWith( compareBy( { it.triggerId }, { it.taskName }, { it.targetDeviceRoleName } ) ).toTypedArray().contentDeepHashCode()

        return result
    }
}