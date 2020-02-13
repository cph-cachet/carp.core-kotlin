package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.DeploymentRepository
import dk.cachet.carp.deployment.domain.StudyDeployment


/**
 * A [DeploymentRepository] which holds study deployments and participations in memory as long as the instance is held in memory.
 */
class InMemoryDeploymentRepository : DeploymentRepository
{
    private val studyDeployments: MutableMap<UUID, StudyDeployment> = mutableMapOf()


    /**
     * Adds the specified [studyDeployment] to the repository.
     *
     * @throws IllegalArgumentException when a study deployment with the same id already exists.
     */
    override fun add( studyDeployment: StudyDeployment )
    {
        require( !studyDeployments.contains( studyDeployment.id ) ) { "The repository already contains a study deployment with ID '${studyDeployment.id}'." }

        studyDeployments[ studyDeployment.id ] = studyDeployment
    }

    /**
     * Return the [StudyDeployment] with the specified [id], or null when no study deployment is found.
     *
     * @param id The id of the [StudyDeployment] to search for.
     */
    override fun getStudyDeploymentBy( id: UUID ): StudyDeployment? = studyDeployments[ id ]

    /**
     * Update a [studyDeployment] which is already stored in this repository.
     *
     * @param studyDeployment The updated version of the study deployment to store.
     * @throws IllegalArgumentException when no previous version of this study deployment is stored in the repository.
     */
    override fun update( studyDeployment: StudyDeployment )
    {
        require( studyDeployments.contains( studyDeployment.id ) ) { "The repository does not contain an existing study deployment with ID '${studyDeployment.id}'." }

        studyDeployments[ studyDeployment.id ] = studyDeployment
    }
}
