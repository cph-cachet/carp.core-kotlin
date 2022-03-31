package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.tasks.TaskConfiguration
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.triggers.TriggerConfiguration
import dk.cachet.carp.common.application.users.ExpectedParticipantData
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
    override val id: UUID,
    override val createdOn: Instant,
    val ownerId: UUID,
    val name: String,
    val description: String? = null,
    val primaryDevices: Set<AnyPrimaryDeviceConfiguration> = emptySet(),
    val connectedDevices: Set<AnyDeviceConfiguration> = emptySet(),
    val connections: Set<DeviceConnection> = emptySet(),
    val tasks: Set<TaskConfiguration<*>> = emptySet(),
    val triggers: Map<Int, TriggerConfiguration<*>> = emptyMap(),
    val taskControls: Set<TaskControl> = emptySet(),
    val expectedParticipantData: Set<ExpectedParticipantData> = emptySet(),
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
                id = protocol.id,
                createdOn = protocol.createdOn,
                ownerId = protocol.ownerId,
                name = protocol.name,
                description = protocol.description,
                primaryDevices = protocol.primaryDevices.toSet(),
                connectedDevices = protocol.devices.minus( protocol.primaryDevices ).toSet(),
                connections = protocol.primaryDevices.flatMap { getConnections( protocol, it ) }.toSet(),
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

        private fun getConnections( protocol: StudyProtocol, primaryDevice: AnyPrimaryDeviceConfiguration ): Iterable<DeviceConnection>
        {
            val connections: MutableList<DeviceConnection> = mutableListOf()

            protocol.getConnectedDevices( primaryDevice ).forEach {
                connections.add( DeviceConnection( it.roleName, primaryDevice.roleName ) )
                if ( it is AnyPrimaryDeviceConfiguration )
                {
                    connections.addAll( getConnections( protocol, it ) )
                }
            }

            return connections
        }
    }


    override fun toObject(): StudyProtocol = StudyProtocol.fromSnapshot( this )
}
