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
     * Return the [ParticipantGroup] for the specified [studyDeploymentId].
     *
     * @throws IllegalArgumentException when no participant group for the specified [studyDeploymentId] is found.
     */
    suspend fun getParticipantGroupOrThrowBy( studyDeploymentId: UUID ): ParticipantGroup =
        getParticipantGroup( studyDeploymentId )
            ?: throw IllegalArgumentException( "A participant group for the study deployment with ID '$studyDeploymentId' does not exist." )

    /**
     * Return all [ParticipantGroup]s matching the specified [studyDeploymentIds].
     * Ids that are not found are ignored.
     */
    suspend fun getParticipantGroupList( studyDeploymentIds: Set<UUID> ): List<ParticipantGroup>

    /**
     * Return all [ParticipantGroup]s matching the specified [studyDeploymentIds].
     *
     * @throws IllegalArgumentException when no participant group exists for one of the passed [studyDeploymentIds].
     */
    suspend fun getParticipantGroupListOrThrow( studyDeploymentIds: Set<UUID> ): List<ParticipantGroup>
    {
        val groups = getParticipantGroupList( studyDeploymentIds )
        require( studyDeploymentIds.size == groups.size ) { "A study deployment ID has been passed for which no participant group exists." }

        return groups
    }

    /**
     * Adds or updates the participant [group] in this repository.
     *
     * @return the previous [ParticipantGroup] stored in the repository, or null if it was not present before.
     */
    suspend fun putParticipantGroup( group: ParticipantGroup ): ParticipantGroup?
}
