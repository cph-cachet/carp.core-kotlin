package dk.cachet.carp.clients.domain

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
     * Adds the specified [study] to the repository.
     *
     * @throws IllegalArgumentException when a [Study] which has the same study deployment ID and device role name already exists.
     */
    suspend fun addStudy( study: Study )

    /**
     * Return the [Study] with [studyDeploymentId] and [deviceRoleName], or null when no such [Study] is found.
     */
    suspend fun getStudyBy( studyDeploymentId: UUID, deviceRoleName: String ): Study?

    /**
     * Return all [Study]s for the client.
     */
    suspend fun getStudyList(): List<Study>

    /**
     * Update a [study] which is already stored in the repository.
     *
     * @throws IllegalArgumentException when no previous version of this study is stored in the repository.
     */
    suspend fun updateStudy( study: Study )

    /**
     * Remove a [study] which is already stored in the repository.
     * In case [study] is not stored in this repository, nothing happens.
     */
    suspend fun removeStudy( study: Study )
}
