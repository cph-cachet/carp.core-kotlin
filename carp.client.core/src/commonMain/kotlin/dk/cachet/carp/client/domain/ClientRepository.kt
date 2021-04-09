package dk.cachet.carp.client.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration


/**
 * A repository which handles persisting the state of client devices.
 */
interface ClientRepository
{
    /**
     * Get the [DeviceRegistration] used to register the client in deployments.
     */
    suspend fun getDeviceRegistration(): DeviceRegistration?

    /**
     * Set the [DeviceRegistration] used to register the client in deployments.
     */
    suspend fun setDeviceRegistration( registration: DeviceRegistration )

    /**
     * Adds the specified [studyRuntime] to the repository.
     *
     * @throws IllegalArgumentException when a [StudyRuntime] which has the same study deployment ID and device role name already exists.
     */
    suspend fun addStudyRuntime( studyRuntime: StudyRuntime )

    /**
     * Return the [StudyRuntime] with [studyDeploymentId] and [deviceRoleName], or null when no such [StudyRuntime] is found.
     */
    suspend fun getStudyRuntimeBy( studyDeploymentId: UUID, deviceRoleName: String ): StudyRuntime?

    /**
     * Return all [StudyRuntime]s for the client.
     */
    suspend fun getStudyRuntimeList(): List<StudyRuntime>

    /**
     * Update a [StudyRuntime] which is already stored in the repository.
     *
     * @throws IllegalArgumentException when no previous version of this study runtime is stored in the repository.
     */
    suspend fun updateStudyRuntime( runtime: StudyRuntime )
}
