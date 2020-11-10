package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.ParticipationRepository


/**
 * A [ParticipationRepository] which holds participations in memory as long as the instance is held in memory.
 */
class InMemoryParticipationRepository : ParticipationRepository
{
    private val participationInvitations: MutableMap<UUID, MutableSet<ParticipationInvitation>> = mutableMapOf()


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
}
