package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.Participation


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
     * Add [participation] information for a study deployment that an account with the given [accountId] should participate in.
     *
     * @param accountId The ID of the account which acts as a [Participation] in a study.
     * @param participation The [Participation] information of the study to participate in.
     */
    fun addParticipation( accountId: UUID, participation: Participation )

    /**
     * Get [Participation] information for all study deployments an account with the given [accountId] participates in.
     */
    fun getParticipations( accountId: UUID ): List<Participation>

    /**
     * Get all participations included in a study deployment for the given [studyDeploymentId].
     */
    fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation>
}
