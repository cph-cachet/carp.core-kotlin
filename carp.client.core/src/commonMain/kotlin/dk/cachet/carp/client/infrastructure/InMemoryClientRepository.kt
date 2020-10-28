package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.domain.ClientRepository
import dk.cachet.carp.client.domain.StudyRuntime
import dk.cachet.carp.client.domain.StudyRuntimeSnapshot
import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * A [ClientRepository] which holds [StudyRuntime]s in memory as long as the instance is held in memory.
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

    private val studyRuntimes: MutableList<StudyRuntimeSnapshot> = mutableListOf()

    /**
     * Adds the specified [studyRuntime] to the repository.
     *
     * @throws IllegalArgumentException when a [StudyRuntime] which has the same study deployment ID and device role name already exists.
     */
    override suspend fun addStudyRuntime( studyRuntime: StudyRuntime )
    {
        val deploymentId = studyRuntime.studyDeploymentId
        val deviceRoleName = studyRuntime.id.deviceRoleName
        require( studyRuntimes.none { it.studyDeploymentId == deploymentId && it.device.roleName == deviceRoleName } )

        studyRuntimes.add( studyRuntime.getSnapshot() )
    }

    /**
     * Return the [StudyRuntime] with [studyDeploymentId] and [deviceRoleName], or null when no such [StudyRuntime] is found.
     */
    override suspend fun getStudyRuntimeBy( studyDeploymentId: UUID, deviceRoleName: String ): StudyRuntime? =
        studyRuntimes
            .filter { it.studyDeploymentId == studyDeploymentId && it.device.roleName == deviceRoleName }
            .map { StudyRuntime.fromSnapshot( it ) }
            .firstOrNull()

    /**
     * Return all [StudyRuntime]s for the client.
     */
    override suspend fun getStudyRuntimeList(): List<StudyRuntime> =
        studyRuntimes.map { StudyRuntime.fromSnapshot( it ) }

    /**
     * Update a [StudyRuntime] which is already stored in the repository.
     *
     * @throws IllegalArgumentException when no previous version of this study runtime is stored in the repository.
     */
    override suspend fun updateStudyRuntime( runtime: StudyRuntime )
    {
        val storedRuntime = studyRuntimes.firstOrNull {
            it.studyDeploymentId == runtime.studyDeploymentId &&
            it.device.roleName == runtime.id.deviceRoleName }
        requireNotNull( storedRuntime ) { "The repository does not contain an existing study runtime matching the one to update." }

        studyRuntimes.remove( storedRuntime )
        studyRuntimes.add( runtime.getSnapshot() )
    }

    override var isDataCollectionPaused: Boolean = true
}
