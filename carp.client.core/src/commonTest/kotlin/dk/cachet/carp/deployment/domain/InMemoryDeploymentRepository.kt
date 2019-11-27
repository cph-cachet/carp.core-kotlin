package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID


/**
 * A [DeploymentRepository] which hold study deployments in memory as long as the instance is held in memory.
 *
 * TODO: Can this be tested using `DeploymentRepositoryTest` from `carp.deployment.core`?
 */
class InMemoryDeploymentRepository : DeploymentRepository
{
    private val _deployments: MutableList<StudyDeployment> = mutableListOf()

    /**
     * Adds the specified [studyDeployment] to the repository.
     */
    override fun add( studyDeployment: StudyDeployment )
    {
        _deployments.add( studyDeployment )
    }

    /**
     * Find the [StudyDeployment] with the specified [id].
     *
     * @param id The id of the [StudyDeployment] to search for.
     * @throws IllegalArgumentException when a study deployment with [id] does not exist.
     */
    override fun getStudyDeploymentBy( id: UUID ): StudyDeployment
    {
        val deployment = _deployments.firstOrNull { it.id == id }
        require( deployment != null ) { "The study deployment with ID \"$id\" does not exist." }

        return deployment
    }

    /**
     * Update a [studyDeployment] which is already stored in this repository.
     *
     * @param studyDeployment The updated version of the study deployment to store.
     */
    override fun update( studyDeployment: StudyDeployment )
    {
        val matchingDeployment = _deployments.firstOrNull { it.id == studyDeployment.id }
        require( matchingDeployment != null ) { "The passed study deployment is not yet stored in this repository." }

        _deployments.remove( matchingDeployment )
        _deployments.add( studyDeployment )
    }
}
