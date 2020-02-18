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
    fun add( studyDeployment: StudyDeployment )

    /**
     * Return the [StudyDeployment] with the specified [id], or null when no study deployment is found.
     *
     * @param id The id of the [StudyDeployment] to search for.
     */
    fun getStudyDeploymentBy( id: UUID ): StudyDeployment?

    /**
     * Update a [studyDeployment] which is already stored in this repository.
     *
     * @param studyDeployment The updated version of the study deployment to store.
     * @throws IllegalArgumentException when no previous version of this study deployment is stored in the repository.
     */
    fun update( studyDeployment: StudyDeployment )

    /**
     * Add a participation [invitation] for an account with the given [accountId].
     */
    fun addInvitation( accountId: UUID, invitation: ParticipationInvitation )

    /**
     * Get all participation invitations for the account with the specified [accountId].
     */
    fun getInvitations( accountId: UUID ): Set<ParticipationInvitation>
}
