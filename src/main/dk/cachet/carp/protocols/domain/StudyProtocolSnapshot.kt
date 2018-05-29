package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.serialization.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.triggers.*
import kotlinx.serialization.*
import kotlinx.serialization.json.JSON
import java.util.*


// Custom serializers for StudyProtocolSnapshot which enable deserializing types that are unknown at runtime, yet extend from a known base type.
private object MasterDevicesSerializer : CustomReferenceArraySerializer<MasterDeviceDescriptor>(
    MasterDeviceDescriptor::class,
    createUnknownPolymorphicSerializer( { className, json -> CustomMasterDeviceDescriptor( className, json ) } )
)


/**
 * A serializable snapshot of a [StudyProtocol] at the moment in time when it was created.
 */
@Serializable
data class StudyProtocolSnapshot(
    val ownerId: String,
    val name: String,
    @Serializable( with = MasterDevicesSerializer::class )
    val masterDevices: Array<MasterDeviceDescriptor>,
    val connectedDevices: Array<DeviceDescriptor>,
    val connections: Array<DeviceConnection>,
    val tasks: Array<TaskDescriptor>,
    val triggers: Array<TriggerWithId>,
    val triggeredTasks: Array<TriggeredTask> )
{
    @Serializable
    data class DeviceConnection( val roleName: String, val connectedToRoleName: String )

    @Serializable
    data class TriggerWithId( val id: Int, val trigger: Trigger )

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
            // TODO: Normally, the serializer does not need to be passed manually as it is inferred by the library.
            //       This is a bug in kotlinx.serialization 0.5 which will be fixed in the next release.
            val serializer = serializer()

            return JSON.parse( serializer, json )
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
        // TODO: Normally, the serializer does not need to be passed manually as it is inferred by the library.
        //       This is a bug in kotlinx.serialization 0.5 which will be fixed in the next release.
        val serializer = serializer()

        return JSON.stringify( serializer, this )
    }


    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false
        other as StudyProtocolSnapshot

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
        result = 31 * result + Arrays.hashCode( masterDevices.sortedWith( compareBy( { it.roleName } ) ).toTypedArray() )
        result = 31 * result + Arrays.hashCode( connectedDevices.sortedWith( compareBy( { it.roleName } ) ).toTypedArray() )
        result = 31 * result + Arrays.hashCode( connections.sortedWith( compareBy( { it.roleName }, { it.connectedToRoleName } ) ).toTypedArray() )
        result = 31 * result + Arrays.hashCode( tasks.sortedWith( compareBy( { it.name } ) ).toTypedArray() )
        result = 31 * result + Arrays.hashCode( triggers.sortedWith( compareBy( { it.id } ) ).toTypedArray() )
        result = 31 * result + Arrays.hashCode( triggeredTasks.sortedWith( compareBy( { it.triggerId }, { it.taskName }, { it.targetDeviceRoleName } ) ).toTypedArray() )

        return result
    }
}