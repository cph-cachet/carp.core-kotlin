package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.UUID


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
     * Remove the [StudyDeployment]s with the specified [studyDeploymentIds].
     *
     * @return The IDs of study deployments which were removed. IDs for which no study deployment exists are ignored.
     */
    suspend fun remove( studyDeploymentIds: Set<UUID> ): Set<UUID>
}
