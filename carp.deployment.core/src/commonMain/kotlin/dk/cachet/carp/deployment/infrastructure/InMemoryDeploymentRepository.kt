package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.infrastructure.InMemoryRepositoryKeyCollection
import dk.cachet.carp.common.infrastructure.RepositoryKeyCollection
import dk.cachet.carp.deployment.domain.DeploymentRepository
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.users.AccountParticipation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * A [DeploymentRepository] which holds study deployments and participations in memory as long as the instance is held in memory.
 */
class InMemoryDeploymentRepository : DeploymentRepository
{
    private val studyDeployments: MutableMap<UUID, StudyDeployment> = mutableMapOf()
    private val invitations: RepositoryKeyCollection<UUID, ParticipationInvitation> =
        InMemoryRepositoryKeyCollection()
    private val participations: RepositoryKeyCollection<UUID, AccountParticipation> =
        InMemoryRepositoryKeyCollection { it in studyDeployments }


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
    override fun getStudyDeploymentBy( id: UUID ): StudyDeployment? =
        studyDeployments[ id ]?.getSnapshot()?.copy(participations = participations.getAll( id ).toSet())?.let { StudyDeployment.fromSnapshot(it) }

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

    override fun addInvitation( accountId: UUID, invitation: ParticipationInvitation )
    {
        invitations.addSingle( accountId, invitation )
    }

    override fun getInvitations( accountId: UUID ): Set<ParticipationInvitation> =
        invitations.getAll( accountId ).toSet()


    override fun registerDevice( studyDeploymentId: UUID, descriptor: AnyDeviceDescriptor, registration: DeviceRegistration )
    {
        require( studyDeployments.contains( studyDeploymentId ) )

        studyDeployments[studyDeploymentId]!!.registerDevice( descriptor, registration )
    }

    override fun addParticipation( studyDeploymentId: UUID, accountParticipation: AccountParticipation )
    {
        participations.addSingle( studyDeploymentId, accountParticipation )
    }
}
