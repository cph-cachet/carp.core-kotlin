package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptorSerializer
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptorSerializer
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptorSerializer
import dk.cachet.carp.protocols.domain.triggers.Trigger
import dk.cachet.carp.protocols.domain.triggers.TriggerSerializer
import kotlinx.serialization.Serializable


/**
 * A serializable snapshot of a [StudyProtocol] at the moment in time when it was created.
 */
@Serializable
data class StudyProtocolSnapshot(
    val ownerId: UUID,
    val name: String,
    val masterDevices: List<@Serializable( MasterDeviceDescriptorSerializer::class ) AnyMasterDeviceDescriptor>,
    val connectedDevices: List<@Serializable( DeviceDescriptorSerializer::class ) AnyDeviceDescriptor>,
    val connections: List<DeviceConnection>,
    val tasks: List<@Serializable( TaskDescriptorSerializer::class ) TaskDescriptor>,
    val triggers: Map<Int, @Serializable( TriggerSerializer::class ) Trigger>,
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
            val triggers = protocol.triggers
                .sortedBy { it.toString() } // Sort so that the order triggers were added in does not impact snapshot equality.
                .associateBy { curTriggerId++ }

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
                    .map { (trigger, taskInfo) ->
                        TriggeredTask( trigger.key, taskInfo.task.name, taskInfo.targetDevice.roleName ) }
                    .toList()
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

        val listsToCompare = listOf(
            masterDevices to other.masterDevices,
            connectedDevices to other.connectedDevices,
            connections to other.connections,
            tasks to other.tasks,
            triggers.toList() to other.triggers.toList(),
            triggeredTasks to other.triggeredTasks
        )
        val allListsMatch = listsToCompare.all { listEquals( it.first, it.second ) }
        if ( !allListsMatch ) return false

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
