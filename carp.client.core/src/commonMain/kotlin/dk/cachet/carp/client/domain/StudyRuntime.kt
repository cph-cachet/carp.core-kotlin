package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentManager
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Manage data collection for a particular study on a client device.
 *
 * @param deploymentManager The application service to use to retrieve and manage the study deployment with [studyDeploymentId].
 * @param studyDeploymentId The ID of the deployed study for which to collect data.
 * @param deviceRoleName The role which the client device this runtime is intended for plays in the deployment identified by [studyDeploymentId].
 * @param deviceRegistration The device configuration for the device this study runtime runs on, identified by [deviceRoleName] in the study deployment with [studyDeploymentId].
 */
class StudyRuntime internal constructor(
    private val deploymentManager: DeploymentManager,
    private val studyDeploymentId: UUID,
    private val deviceRoleName: String,
    deviceRegistration: DeviceRegistration )
{
    init
    {
        // Register the client device this study runs on for the given study deployment.
        deploymentManager.registerDevice( studyDeploymentId, deviceRoleName, deviceRegistration )
    }
}