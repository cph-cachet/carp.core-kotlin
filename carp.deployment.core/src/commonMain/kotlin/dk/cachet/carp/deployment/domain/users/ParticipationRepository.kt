package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID


interface ParticipationRepository
{
    /**
     * Add a participation [invitation] for an account with the given [accountId].
     */
    suspend fun addInvitation( accountId: UUID, invitation: ParticipationInvitation )

    /**
     * Get all participation invitations for the account with the specified [accountId].
     */
    suspend fun getInvitations( accountId: UUID ): Set<ParticipationInvitation>

    /**
     * Returns the [ParticipantGroup] for the specified [studyDeploymentId], or null when it is not found.
     */
    suspend fun getParticipantGroup( studyDeploymentId: UUID ): ParticipantGroup?

    /**
     * Return all [ParticipantGroup]s maching the specified [studyDeploymentIds].
     * Ids that are not found are ignored.
     */
    suspend fun getParticipantGroupList( studyDeploymentIds: Set<UUID> ): List<ParticipantGroup>

    /**
     * Adds or updates the participant [group] in this repository.
     *
     * @return the previous [ParticipantGroup] stored in the repository, or null if it was not present before.
     */
    suspend fun putParticipantGroup( group: ParticipantGroup ): ParticipantGroup?
}
