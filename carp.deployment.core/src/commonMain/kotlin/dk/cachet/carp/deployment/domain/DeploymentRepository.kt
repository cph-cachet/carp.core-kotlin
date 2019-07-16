package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID


interface DeploymentRepository
{
    /**
     * Adds the specified [deployment] to the repository.
     */
    fun add( deployment: StudyDeployment )

    /**
     * Find the [StudyDeployment] with the specified [id].
     *
     * @param id The id of the [StudyDeployment] to search for.
     * @throws IllegalArgumentException when a deployment with [id] does not exist.
     */
    fun getBy( id: UUID ): StudyDeployment

    /**
     * Update a [deployment] which is already stored in this repository.
     *
     * @param deployment The updated version of the deployment to store.
     */
    fun update( deployment: StudyDeployment )
}