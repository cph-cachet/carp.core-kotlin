package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.common.infrastructure.serialization.ApplicationDataSerializer
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * A serializable snapshot of a [StudyProtocol] at the moment in time when it was created.
 */
@Serializable
data class StudyProtocolSnapshot(
    val id: StudyProtocolId,
    val description: String,
    override val creationDate: Instant,
    val masterDevices: List<AnyMasterDeviceDescriptor>,
    val connectedDevices: List<AnyDeviceDescriptor>,
    val connections: List<DeviceConnection>,
    val tasks: List<TaskDescriptor>,
    val triggers: Map<Int, Trigger<*>>,
    val taskControls: List<TaskControl>,
    val expectedParticipantData: List<ParticipantAttribute>,
    @Serializable( ApplicationDataSerializer::class )
    val applicationData: String
) : Snapshot<StudyProtocol>
{
    @Serializable
    data class DeviceConnection( val roleName: String, val connectedToRoleName: String )

    companion object
    {
        /**
         * Create a snapshot of the specified [StudyProtocol].
         *
         * @param protocol The [StudyProtocol] to create a snapshot for.
         */
        fun fromProtocol( protocol: StudyProtocol ): StudyProtocolSnapshot
        {
            val triggers = protocol.triggers.associate { it.id to it.trigger }

            return StudyProtocolSnapshot(
                protocol.id,
                description = protocol.description,
                creationDate = protocol.creationDate,
                masterDevices = protocol.masterDevices.toList(),
                connectedDevices = protocol.devices.minus( protocol.masterDevices ).toList(),
                connections = protocol.masterDevices.flatMap { getConnections( protocol, it ) }.toList(),
                tasks = protocol.tasks.toList(),
                triggers = triggers,
                taskControls = triggers
                    .flatMap { trigger -> protocol.getTaskControls( trigger.value ).map { trigger to it } }
                    .map { (trigger, control) ->
                        TaskControl( trigger.key, control.task.name, control.destinationDevice.roleName, control.control ) }
                    .toList(),
                expectedParticipantData = protocol.expectedParticipantData.toList(),
                applicationData = protocol.applicationData
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

        if ( id != other.id ) return false
        if ( description != other.description ) return false
        if ( creationDate != other.creationDate ) return false
        if ( applicationData != other.applicationData ) return false

        val listsToCompare = listOf(
            masterDevices to other.masterDevices,
            connectedDevices to other.connectedDevices,
            connections to other.connections,
            tasks to other.tasks,
            triggers.toList() to other.triggers.toList(),
            taskControls to other.taskControls,
            expectedParticipantData.toList() to other.expectedParticipantData.toList()
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
        var result = id.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + creationDate.hashCode()
        result = 31 * result + applicationData.hashCode()
        result = 31 * result + masterDevices.sortedWith( compareBy { it.roleName } ).toTypedArray().contentDeepHashCode()
        result = 31 * result + connectedDevices.sortedWith( compareBy { it.roleName } ).toTypedArray().contentDeepHashCode()
        result = 31 * result + connections.sortedWith( compareBy( { it.roleName }, { it.connectedToRoleName } ) ).toTypedArray().contentDeepHashCode()
        result = 31 * result + tasks.sortedWith( compareBy { it.name } ).toTypedArray().contentDeepHashCode()
        result = 31 * result + triggers.entries.sortedWith( compareBy { it.key } ).toTypedArray().contentDeepHashCode()
        result = 31 * result + taskControls.sortedWith( compareBy( { it.triggerId }, { it.taskName }, { it.destinationDeviceRoleName } ) ).toTypedArray().contentDeepHashCode()
        result = 31 * result + expectedParticipantData.sortedWith( compareBy { it.inputType.toString() } ).toTypedArray().contentDeepHashCode()

        return result
    }

    override fun toObject(): StudyProtocol = StudyProtocol.fromSnapshot( this )
}
