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
    val description: String? = null,
    override val createdOn: Instant,
    val masterDevices: Set<AnyMasterDeviceDescriptor> = emptySet(),
    val connectedDevices: Set<AnyDeviceDescriptor> = emptySet(),
    val connections: Set<DeviceConnection> = emptySet(),
    val tasks: Set<TaskDescriptor> = emptySet(),
    val triggers: Map<Int, Trigger<*>> = emptyMap(),
    val taskControls: Set<TaskControl> = emptySet(),
    val expectedParticipantData: Set<ParticipantAttribute> = emptySet(),
    @Serializable( ApplicationDataSerializer::class )
    val applicationData: String? = null
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
                createdOn = protocol.createdOn,
                masterDevices = protocol.masterDevices.toSet(),
                connectedDevices = protocol.devices.minus( protocol.masterDevices ).toSet(),
                connections = protocol.masterDevices.flatMap { getConnections( protocol, it ) }.toSet(),
                tasks = protocol.tasks.toSet(),
                triggers = triggers,
                taskControls = triggers
                    .flatMap { trigger -> protocol.getTaskControls( trigger.value ).map { trigger to it } }
                    .map { (trigger, control) ->
                        TaskControl( trigger.key, control.task.name, control.destinationDevice.roleName, control.control ) }
                    .toSet(),
                expectedParticipantData = protocol.expectedParticipantData.toSet(),
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


    override fun toObject(): StudyProtocol = StudyProtocol.fromSnapshot( this )
}
