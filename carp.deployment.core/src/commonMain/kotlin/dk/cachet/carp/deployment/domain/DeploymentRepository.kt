package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation


interface DeploymentRepository
{
    /**
     * Adds the specified [studyDeployment] to the repository.
     *
     * @throws IllegalArgumentException when a study deployment with the same id already exists.
     */
    suspend fun add( studyDeployment: StudyDeployment )

    /**
     * Return the [StudyDeployment] with the specified [id], or null when no study deployment is found.
     */
    suspend fun getStudyDeploymentBy( id: UUID ): StudyDeployment? = getStudyDeploymentsBy( setOf( id ) ).firstOrNull()

    /**
     * Return the [StudyDeployment] with the specified [id].
     *
     * @throws IllegalArgumentException when no study deployment is found.
     */
    suspend fun getStudyDeploymentOrThrowBy( id: UUID ): StudyDeployment = getStudyDeploymentBy( id )
        ?: throw IllegalArgumentException( "A deployment with ID '$id' does not exist." )

    /**
     * Return all [StudyDeployment]s matching any of the specified [ids].
     * Ids that are not found are ignored.
     */
    suspend fun getStudyDeploymentsBy( ids: Set<UUID> ): List<StudyDeployment>

    /**
     * Update a [studyDeployment] which is already stored in this repository.
     *
     * @param studyDeployment The updated version of the study deployment to store.
     * @throws IllegalArgumentException when no previous version of this study deployment is stored in the repository.
     */
    suspend fun update( studyDeployment: StudyDeployment )

    /**
     * Add a participation [invitation] for an account with the given [accountId].
     */
    suspend fun addInvitation( accountId: UUID, invitation: ParticipationInvitation )

    /**
     * Get all participation invitations for the account with the specified [accountId].
     */
    suspend fun getInvitations( accountId: UUID ): Set<ParticipationInvitation>
}
