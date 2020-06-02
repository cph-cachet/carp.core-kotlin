package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.DeploymentRepository
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation


/**
 * A [DeploymentRepository] which holds study deployments and participations in memory as long as the instance is held in memory.
 */
class InMemoryDeploymentRepository : DeploymentRepository
{
    private val studyDeployments: MutableMap<UUID, StudyDeploymentSnapshot> = mutableMapOf()
    private val participationInvitations: MutableMap<UUID, MutableSet<ParticipationInvitation>> = mutableMapOf()


    /**
     * Adds the specified [studyDeployment] to the repository.
     *
     * @throws IllegalArgumentException when a study deployment with the same id already exists.
     */
    override suspend fun add( studyDeployment: StudyDeployment )
    {
        require( !studyDeployments.contains( studyDeployment.id ) ) { "The repository already contains a study deployment with ID '${studyDeployment.id}'." }

        studyDeployments[ studyDeployment.id ] = studyDeployment.getSnapshot()
    }

    /**
     * Return all [StudyDeployment]s matching any of the specified [ids].
     * Ids that are not found are ignored.
     */
    override suspend fun getStudyDeploymentsBy( ids: Set<UUID> ): List<StudyDeployment> =
        studyDeployments
            .filterKeys { it in ids }
            .map { StudyDeployment.fromSnapshot( it.value ) }
            .toList()

    /**
     * Update a [studyDeployment] which is already stored in this repository.
     *
     * @param studyDeployment The updated version of the study deployment to store.
     * @throws IllegalArgumentException when no previous version of this study deployment is stored in the repository.
     */
    override suspend fun update( studyDeployment: StudyDeployment )
    {
        require( studyDeployments.contains( studyDeployment.id ) ) { "The repository does not contain an existing study deployment with ID '${studyDeployment.id}'." }

        studyDeployments[ studyDeployment.id ] = studyDeployment.getSnapshot()
    }

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
