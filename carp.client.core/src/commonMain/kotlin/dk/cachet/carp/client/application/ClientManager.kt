package dk.cachet.carp.client.application

import dk.cachet.carp.client.domain.StudyRuntime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentManager
import dk.cachet.carp.protocols.domain.devices.*


/**
 * Application service which allows managing [StudyRuntime]'s on a client device.
 *
 * @param deviceRegistration The device configuration for this client device, used to register for study deployments managed by the [deploymentManager].
 * @param deploymentManager The application service through which study deployments can be managed and retrieved.
 */
class ClientManager<TMasterDevice: MasterDeviceDescriptor<TRegistration,*>, TRegistration: DeviceRegistration>(
    private val deviceRegistration: TRegistration,
    private val deploymentManager: DeploymentManager )
{
    private val _studies: MutableList<StudyRuntime> = mutableListOf()
    /**
     * The studies which run on this client device.
     */
    val studies: List<StudyRuntime> = _studies


    /**
     * Add a study which needs to be executed on this client. This involves registering this device for the specified study deployment.
     *
     * @param studyDeploymentId The ID of a study which has been deployed already and for which to collect data.
     * @param deviceRoleName The role which the client device this runtime is intended for plays as part of the deployment identified by [studyDeploymentId].
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist,
     * [deviceRoleName] is not present in the deployment or is already registered,
     * or the [deviceRegistration] of this client is invalid for the specified device or uses a device ID which has already been used as part of registration of a different device.
     * @return The [StudyRuntime] through which data collection for the newly added study can be managed.
     */
    fun addStudy( studyDeploymentId: UUID, deviceRoleName: String ): StudyRuntime
    {
        // Create the study runtime.
        // IllegalArgumentException's will be thrown here when deployment or role name does not exist, or device is already registered.
        val runtime = StudyRuntime( deploymentManager, studyDeploymentId, deviceRoleName, deviceRegistration )

        _studies.add( runtime )
        return runtime
    }
}