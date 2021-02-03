package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.ParticipantGroup
import dk.cachet.carp.deployment.domain.users.ParticipantGroupSnapshot
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.ParticipationRepository


/**
 * A [ParticipationRepository] which holds participations in memory as long as the instance is held in memory.
 */
class InMemoryParticipationRepository : ParticipationRepository
{
    private val participationInvitations: MutableMap<UUID, MutableSet<ParticipationInvitation>> = mutableMapOf()
    private val participantGroups: MutableMap<UUID, ParticipantGroupSnapshot> = mutableMapOf()


    /**
     * Add a participation [invitation] for an account with the given [accountId].
     */
    override suspend fun addInvitation( accountId: UUID, invitation: ParticipationInvitation )
    {
        val invitations = participationInvitations.getOrPut( accountId ) { mutableSetOf() }
        invitations.add( invitation )
    }

    /**
     * Get all participation invitations for the account with the specified [accountId].
     */
    override suspend fun getInvitations( accountId: UUID ): Set<ParticipationInvitation> =
        participationInvitations.getOrElse( accountId ) { setOf() }

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
}
