package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.common.infrastructure.serialization.MapAsArraySerializer
import dk.cachet.carp.deployments.application.users.AssignedPrimaryDevice
import dk.cachet.carp.protocols.application.users.ExpectedParticipantData
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * A serializable snapshot of a [ParticipantGroup] at the moment in time when it was created.
 */
@Serializable
data class ParticipantGroupSnapshot(
    override val id: UUID,
    override val createdOn: Instant,
    val studyDeploymentId: UUID,
    val assignedPrimaryDevices: Set<AssignedPrimaryDevice>,
    val isStudyDeploymentStopped: Boolean,
    val participations: Set<AccountParticipation> = emptySet(),
    @Serializable( MapAsArraySerializer::class )
    val data: Map<ExpectedParticipantData, Data?> = emptyMap(),
) : Snapshot<ParticipantGroup>
{
    companion object
    {
        /**
         * Create a snapshot of the specified participant [group].
         */
        fun fromParticipantGroup( group: ParticipantGroup ): ParticipantGroupSnapshot =
            ParticipantGroupSnapshot(
                group.id,
                group.createdOn,
                group.studyDeploymentId,
                group.assignedPrimaryDevices.toSet(),
                group.isStudyDeploymentStopped,
                group.participations.toSet(),
                group.data.toMap()
            )
    }


    override fun toObject(): ParticipantGroup = ParticipantGroup.fromSnapshot( this )
}
