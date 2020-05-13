package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * A repository which handles persisting the state of client devices.
 */
interface ClientRepository
{
    /**
     * The [DeviceRegistration] used to register the client in deployments.
     */
    var deviceRegistration: DeviceRegistration?

    /**
     * Adds the specified [studyRuntime] to the repository.
     *
     * @throws IllegalArgumentException when a [StudyRuntime] which has the same study deployment ID and device role name already exists.
     */
    fun addStudyRuntime( studyRuntime: StudyRuntime )

    /**
     * Return the [StudyRuntime] with [studyDeploymentId] and [deviceRoleName], or null when no such [StudyRuntime] is found.
     */
    fun getStudyRuntimeBy( studyDeploymentId: UUID, deviceRoleName: String ): StudyRuntime?

    /**
     * Return all [StudyRuntime]s for the client.
     */
    fun getStudyRuntimeList(): List<StudyRuntime>
}
