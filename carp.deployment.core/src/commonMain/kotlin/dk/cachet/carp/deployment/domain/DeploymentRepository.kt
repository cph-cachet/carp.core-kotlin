package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID


interface DeploymentRepository
{
    /**
     * Adds the specified [studyDeployment] to the repository.
     */
    fun add( studyDeployment: StudyDeployment )

    /**
     * Find the [StudyDeployment] with the specified [id].
     *
     * @param id The id of the [StudyDeployment] to search for.
     * @throws IllegalArgumentException when a study deployment with [id] does not exist.
     */
    fun getStudyDeploymentBy( id: UUID ): StudyDeployment

    /**
     * Update a [studyDeployment] which is already stored in this repository.
     *
     * @param studyDeployment The updated version of the study deployment to store.
     */
    fun update( studyDeployment: StudyDeployment )
}