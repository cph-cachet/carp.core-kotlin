package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.DeploymentRepository
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.users.Participation


/**
 * A [DeploymentRepository] which holds study deployments and participations in memory as long as the instance is held in memory.
 */
class InMemoryDeploymentRepository : DeploymentRepository
{
    private val studyDeployments: MutableMap<UUID, StudyDeployment> = mutableMapOf()
    private val participations: MutableMap<UUID, MutableSet<Participation>> = mutableMapOf()

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

    /**
     * Add [participation] information for a study deployment that an account with the given [accountId] should participate in.
     *
     * @param accountId The ID of the account which acts as a [Participation] in a study.
     * @param participation The [Participation] information of the study to participate in.
     */
    override fun addParticipation( accountId: UUID, participation: Participation )
    {
        val accountParticipations = participations.getOrPut( accountId ) { mutableSetOf() }
        accountParticipations.add( participation )
    }

    /**
     * Get [Participation] information for all study deployments an account with the given [accountId] participates in.
     */
    override fun getParticipations( accountId: UUID ): List<Participation> =
        participations[ accountId ]?.toList() ?: listOf()

    /**
     * Get all participations included in a study deployment for the given [studyDeploymentId].
     */
    override fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation> =
        participations.flatMap { it.component2().filter { p -> p.studyDeploymentId == studyDeploymentId } }
}
