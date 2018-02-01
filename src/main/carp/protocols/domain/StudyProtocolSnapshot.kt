package carp.protocols.domain

import carp.protocols.domain.devices.*
import carp.protocols.domain.tasks.*
import carp.protocols.domain.triggers.*
import kotlinx.serialization.Serializable
import java.util.*


/**
 * A serializable snapshot of a [StudyProtocol] at the moment in time when it was created.
 */
@Serializable
data class StudyProtocolSnapshot(
    val ownerId: String,
    val name: String,
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


    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false
        other as StudyProtocolSnapshot

        if ( ownerId != other.ownerId ) return false
        if ( name != other.name ) return false

        // TODO: The order of the array should not matter here.
        if ( !Arrays.equals( masterDevices, other.masterDevices ) ) return false
        if ( !Arrays.equals( connectedDevices, other.connectedDevices ) ) return false
        if ( !Arrays.equals( connections, other.connections ) ) return false
        if ( !Arrays.equals( tasks, other.tasks ) ) return false
        if ( !Arrays.equals( triggers, other.triggers ) ) return false
        if ( !Arrays.equals( triggeredTasks, other.triggeredTasks ) ) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = ownerId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + Arrays.hashCode( masterDevices )
        result = 31 * result + Arrays.hashCode( connectedDevices )
        result = 31 * result + Arrays.hashCode( connections )
        result = 31 * result + Arrays.hashCode( tasks )
        result = 31 * result + Arrays.hashCode( triggers )
        result = 31 * result + Arrays.hashCode( triggeredTasks )

        return result
    }
}