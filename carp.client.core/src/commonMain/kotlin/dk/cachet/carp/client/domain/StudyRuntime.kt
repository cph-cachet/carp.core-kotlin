package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentManager
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Manage data collection for a particular study on a client device.
 */
class StudyRuntime internal constructor(
    /**
     * The application service to use to retrieve and manage the study deployment with [studyDeploymentId].
     */
    private val deploymentManager: DeploymentManager,
    /**
     * The ID of the deployed study for which to collect data.
     */
    val studyDeploymentId: UUID,
    /**
     * The role which the client device this runtime is intended for plays in the deployment identified by [studyDeploymentId].
     */
    val deviceRoleName: String )
{
    companion object Factory
    {
        /**
         * Instantiate and initialize a [StudyRuntime] by registering the client device in the [deploymentManager].
         */
        internal fun initialize(
            /**
             * The application service to use to retrieve and manage the study deployment with [studyDeploymentId].
             */
            deploymentManager: DeploymentManager,
            /**
             * The ID of the deployed study for which to collect data.
             */
            studyDeploymentId: UUID,
            /**
             * The role which the client device this runtime is intended for plays in the deployment identified by [studyDeploymentId].
             */
            deviceRoleName: String,
            /**
             * The device configuration for the device this study runtime runs on, identified by [deviceRoleName] in the study deployment with [studyDeploymentId].
             */
            deviceRegistration: DeviceRegistration ): StudyRuntime
        {
            // Register the client device this study runs on for the given study deployment.
            deploymentManager.registerDevice( studyDeploymentId, deviceRoleName, deviceRegistration )

            return StudyRuntime( deploymentManager, studyDeploymentId, deviceRoleName )
        }
    }
}