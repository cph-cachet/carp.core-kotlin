package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.ddd.Snapshot
import dk.cachet.carp.common.serialization.MapAsArraySerializer
import dk.cachet.carp.common.users.ParticipantAttribute
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
