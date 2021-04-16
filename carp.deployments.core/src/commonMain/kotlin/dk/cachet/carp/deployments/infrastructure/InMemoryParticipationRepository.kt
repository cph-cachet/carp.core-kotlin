package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.domain.users.AccountParticipation
import dk.cachet.carp.deployments.domain.users.ParticipantGroup
import dk.cachet.carp.deployments.domain.users.ParticipantGroupSnapshot
import dk.cachet.carp.deployments.domain.users.ParticipationRepository


/**
 * A [ParticipationRepository] which holds participations in memory as long as the instance is held in memory.
 */
class InMemoryParticipationRepository : ParticipationRepository
{
    private val participantGroups: MutableMap<UUID, ParticipantGroupSnapshot> = mutableMapOf()


    /**
     * Get all participations invitations for the account with the specified [accountId].
     */
    override suspend fun getParticipationInvitations( accountId: UUID ): Set<AccountParticipation> = participantGroups
        .flatMap { it.value.participations }
        .filter { it.accountId == accountId }
        .toSet()

    /**
     * Returns the [ParticipantGroup] for the specified [studyDeploymentId], or null when it is not found.
     */
    override suspend fun getParticipantGroup( studyDeploymentId: UUID ): ParticipantGroup? =
        participantGroups[ studyDeploymentId ]?.let { ParticipantGroup.fromSnapshot( it ) }

    /**
     * Return all [ParticipantGroup]s matching the specified [studyDeploymentIds].
     * Ids that are not found are ignored.
     */
    override suspend fun getParticipantGroupList( studyDeploymentIds: Set<UUID> ): List<ParticipantGroup> =
        participantGroups
            .filter { it.key in studyDeploymentIds }
            .map { ParticipantGroup.fromSnapshot( it.value ) }

    /**
     * Adds or updates the participant [group] in this repository.
     *
     * @return the previous [ParticipantGroup] stored in the repository, or null if it was not present before.
     */
    override suspend fun putParticipantGroup( group: ParticipantGroup ): ParticipantGroup? =
        participantGroups
            .put( group.studyDeploymentId, group.getSnapshot() )
            ?.let { ParticipantGroup.fromSnapshot( it ) }

    /**
     * Remove the [ParticipantGroup]s matching the specified [studyDeploymentIds].
     *
     * @return The IDs of study deployments for which participant groups were removed. IDs for which no participant group exists are ignored.
     */
    override suspend fun removeParticipantGroups( studyDeploymentIds: Set<UUID> ): Set<UUID> =
        studyDeploymentIds
            .mapNotNull { participantGroups.remove( it ) }
            .map { it.studyDeploymentId }
            .toSet()
}
