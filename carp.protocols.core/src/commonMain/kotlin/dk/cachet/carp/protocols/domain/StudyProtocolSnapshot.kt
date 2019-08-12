package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.triggers.*
import kotlinx.serialization.*


/**
 * A serializable snapshot of a [StudyProtocol] at the moment in time when it was created.
 */
@Serializable
data class StudyProtocolSnapshot(
    @Serializable( with = UUIDSerializer::class )
    val ownerId: UUID,
    val name: String,
    @Serializable( MasterDevicesSerializer::class )
    val masterDevices: List<AnyMasterDeviceDescriptor>,
    @Serializable( DevicesSerializer::class )
    val connectedDevices: List<AnyDeviceDescriptor>,
    val connections: List<DeviceConnection>,
    @Serializable( TasksSerializer::class )
    val tasks: List<TaskDescriptor>,
    @Serializable( TriggersIdMapSerializer::class )
    val triggers: Map<Int, Trigger>,
    val triggeredTasks: List<TriggeredTask> )
{
    @Serializable
    data class DeviceConnection( val roleName: String, val connectedToRoleName: String )

    @Serializable
    data class TriggeredTask( val triggerId: Int, val taskName: String, val targetDeviceRoleName: String )

    companion object
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
            val triggers = protocol.triggers.associateBy { curTriggerId++ }

            return StudyProtocolSnapshot(
                ownerId = protocol.owner.id,
                name = protocol.name,
                masterDevices = protocol.masterDevices.toList(),
                connectedDevices = protocol.devices.minus( protocol.masterDevices ).toList(),
                connections = protocol.masterDevices.flatMap { getConnections( protocol, it ) }.toList(),
                tasks = protocol.tasks.toList(),
                triggers = triggers,
                triggeredTasks = triggers
                    .flatMap { trigger -> protocol.getTriggeredTasks( trigger.value ).map { trigger to it } }
                    .map { (trigger, taskInfo) -> TriggeredTask( trigger.key, taskInfo.task.name, taskInfo.device.roleName ) }.toList()
            )
        }

        private fun getConnections( protocol: StudyProtocol, masterDevice: AnyMasterDeviceDescriptor ): Iterable<DeviceConnection>
        {
            val connections: MutableList<DeviceConnection> = mutableListOf()

            protocol.getConnectedDevices( masterDevice ).forEach {
                connections.add( DeviceConnection( it.roleName, masterDevice.roleName ) )
                if ( it is AnyMasterDeviceDescriptor )
                {
                    connections.addAll( getConnections( protocol, it ) )
                }
            }

            return connections
        }
    }


    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( other !is StudyProtocolSnapshot ) return false

        if ( ownerId != other.ownerId ) return false
        if ( name != other.name ) return false

        if ( !listEquals( masterDevices, other.masterDevices ) ) return false
        if ( !listEquals( connectedDevices, other.connectedDevices ) ) return false
        if ( !listEquals( connections, other.connections ) ) return false
        if ( !listEquals( tasks, other.tasks ) ) return false
        if ( !listEquals( triggers.toList(), other.triggers.toList() ) ) return false
        if ( !listEquals( triggeredTasks, other.triggeredTasks ) ) return false

        return true
    }

    private fun <T> listEquals( a1: List<T>, a2: List<T> ): Boolean
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
        result = 31 * result + triggers.entries.sortedWith( compareBy { it.key } ).toTypedArray().contentDeepHashCode()
        result = 31 * result + triggeredTasks.sortedWith( compareBy( { it.triggerId }, { it.taskName }, { it.targetDeviceRoleName } ) ).toTypedArray().contentDeepHashCode()

        return result
    }
}