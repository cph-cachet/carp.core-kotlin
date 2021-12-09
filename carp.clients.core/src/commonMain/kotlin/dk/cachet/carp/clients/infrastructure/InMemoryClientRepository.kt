package dk.cachet.carp.clients.infrastructure

import dk.cachet.carp.clients.domain.ClientRepository
import dk.cachet.carp.clients.domain.study.Study
import dk.cachet.carp.clients.domain.study.StudySnapshot
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration


/**
 * A [ClientRepository] which holds [Study]s in memory as long as the instance is held in memory.
 */
class InMemoryClientRepository : ClientRepository
{
    private var deviceRegistration: DeviceRegistration? = null

    /**
     * Get the [DeviceRegistration] used to register the client in deployments.
     */
    override suspend fun getDeviceRegistration(): DeviceRegistration? = deviceRegistration

    /**
     * Set the [DeviceRegistration] used to register the client in deployments.
     */
    override suspend fun setDeviceRegistration( registration: DeviceRegistration )
    {
        deviceRegistration = registration
    }

    private val studies: MutableList<StudySnapshot> = mutableListOf()

    /**
     * Adds the specified [study] to the repository.
     *
     * @throws IllegalArgumentException when a [Study] which has the same study deployment ID and device role name already exists.
     */
    override suspend fun addStudy( study: Study )
    {
        val deploymentId = study.studyDeploymentId
        val deviceRoleName = study.deviceRoleName
        require( studies.none { it.studyDeploymentId == deploymentId && it.deviceRoleName == deviceRoleName } )

        studies.add( study.getSnapshot() )
    }

    /**
     * Return the [Study] with [studyDeploymentId] and [deviceRoleName], or null when no such [Study] is found.
     */
    override suspend fun getStudyBy( studyDeploymentId: UUID, deviceRoleName: String ): Study? =
        studies
            .filter { it.studyDeploymentId == studyDeploymentId && it.deviceRoleName == deviceRoleName }
            .map { Study.fromSnapshot( it ) }
            .firstOrNull()

    /**
     * Return all [Study]s for the client.
     */
    override suspend fun getStudyList(): List<Study> =
        studies.map { Study.fromSnapshot( it ) }

    /**
     * Update a [study] which is already stored in the repository.
     *
     * @throws IllegalArgumentException when no previous version of this study is stored in the repository.
     */
    override suspend fun updateStudy( study: Study )
    {
        val storedStudy = findStudySnapshot( study )
        requireNotNull( storedStudy ) { "The repository does not contain an existing study matching the one to update." }

        studies.remove( storedStudy )
        studies.add( study.getSnapshot() )
    }

    /**
     * Remove a [study] which is already stored in the repository.
     * In case [study] is not stored in this repository, nothing happens.
     */
    override suspend fun removeStudy( study: Study )
    {
        val storedStudy = findStudySnapshot( study )
        studies.remove( storedStudy )
    }

    private fun findStudySnapshot( runtime: Study ): StudySnapshot? =
        studies.firstOrNull {
            it.studyDeploymentId == runtime.studyDeploymentId &&
            it.deviceRoleName == runtime.deviceRoleName
        }
}
