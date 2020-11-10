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
}
