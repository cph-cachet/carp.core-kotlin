package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.common.infrastructure.serialization.MapAsArraySerializer
import dk.cachet.carp.deployments.application.users.AssignedMasterDevice
import kotlinx.serialization.Serializable


/**
 * A serializable snapshot of a [ParticipantGroup] at the moment in time when it was created.
 */
@Serializable
data class ParticipantGroupSnapshot(
    override val creationDate: DateTime,
    val studyDeploymentId: UUID,
    val assignedMasterDevices: Set<AssignedMasterDevice>,
    val isStudyDeploymentStopped: Boolean,
    val expectedData: Set<ParticipantAttribute>,
    val participations: Set<AccountParticipation>,
    @Serializable( MapAsArraySerializer::class )
    val data: Map<InputDataType, Data?>
) : Snapshot<ParticipantGroup>
{
    companion object
    {
        /**
         * Create a snapshot of the specified participant [group].
         */
        fun fromParticipantGroup( group: ParticipantGroup ): ParticipantGroupSnapshot =
            ParticipantGroupSnapshot(
                group.creationDate,
                group.studyDeploymentId,
                group.assignedMasterDevices.toSet(),
                group.isStudyDeploymentStopped,
                group.expectedData.toSet(),
                group.participations.toSet(),
                group.data.toMap()
            )
    }


    override fun toObject(): ParticipantGroup = ParticipantGroup.fromSnapshot( this )
}
