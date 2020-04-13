package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.domain.DeviceDeploymentStatus
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Manage data collection for a particular study on a client device.
 */
class StudyRuntime private constructor(
    private val deploymentService: DeploymentService,
    /**
     * The ID of the deployed study for which to collect data.
     */
    val studyDeploymentId: UUID,
    /**
     * The description of the device this runtime is intended for within the deployment identified by [studyDeploymentId].
     */
    val device: AnyMasterDeviceDescriptor
)
{
    companion object Factory
    {
        /**
         * Instantiate a [StudyRuntime] by registering the client device in the [deploymentService].
         * In case the device is immediately ready for deployment, also deploy.
         *
         * @throws IllegalArgumentException when:
         * - a deployment with [studyDeploymentId] does not exist
         * - [deviceRoleName] is not present in the deployment or is already registered and a different [deviceRegistration] is specified than a previous request
         * - [deviceRegistration] is invalid for the specified device or uses a device ID which has already been used as part of registration of a different device
         * @throws UnsupportedOperationException in case deployment failed since not all necessary plugins to execute the study are available.
         */
        internal suspend fun initialize(
            /**
             * The application service to use to retrieve and manage the study deployment with [studyDeploymentId].
             * This deployment service should have the deployment with [studyDeploymentId] available.
             */
            deploymentService: DeploymentService,
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
            deviceRegistration: DeviceRegistration
        ): StudyRuntime
        {
            // Register the client device this study runs on for the given study deployment.
            val deploymentStatus = deploymentService.registerDevice( studyDeploymentId, deviceRoleName, deviceRegistration )

            // Initialize runtime.
            val clientDeviceStatus = deploymentStatus.devicesStatus.first { it.device.roleName == deviceRoleName }
            val runtime = StudyRuntime( deploymentService, studyDeploymentId, clientDeviceStatus.device as AnyMasterDeviceDescriptor )

            // After registration, deployment information might immediately be available for this client device.
            runtime.tryDeployment( clientDeviceStatus )

            return runtime
        }

        internal fun fromSnapshot( snapshot: StudyRuntimeSnapshot, deploymentService: DeploymentService ): StudyRuntime =
            StudyRuntime( deploymentService, snapshot.studyDeploymentId, snapshot.device ).apply {
                isDeployed = snapshot.isDeployed
                deploymentInformation = snapshot.deploymentInformation
            }
    }


    /**
     * Determines whether the device has retrieved its [MasterDeviceDeployment] and was able to load all the necessary plugins to execute the study.
     */
    var isDeployed: Boolean = false
        private set

    /**
     * In case deployment succeeded ([isDeployed] is true), this contains all the information on the study to run.
     * TODO: This should be consumed within this domain model and not be public.
     *       Currently, it is in order to work towards a first MVP which includes server/client communication through the domain model.
     */
    var deploymentInformation: MasterDeviceDeployment? = null
        private set

    /**
     * Verifies whether the device is ready for deployment and in case it is, deploys.
     *
     * @return True in case deployment succeeded; false in case device could not yet be deployed (e.g., awaiting registration of other devices).
     * @throws UnsupportedOperationException in case deployment failed since not all necessary plugins to execute the study are available.
     */
    suspend fun tryDeployment(): Boolean
    {
        val deploymentStatus = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val clientDeviceStatus = deploymentStatus.getDeviceStatus( device )

        return tryDeployment( clientDeviceStatus )
    }

    private suspend fun tryDeployment( deviceStatus: DeviceDeploymentStatus ): Boolean
    {
        // Early out in case state indicates the device is not yet ready to deploy.
        if ( deviceStatus !is DeviceDeploymentStatus.NotDeployed || !deviceStatus.isReadyForDeployment ) return false

        // TODO: Get deployment and throw exception in case there are missing plugins to perform the operations (e.g., measurements).
        val deployment = deploymentService.getDeviceDeploymentFor( studyDeploymentId, device.roleName )
        deploymentInformation = deployment

        // Notify deployment service of successful deployment.
        try
        {
            deploymentService.deploymentSuccessful( studyDeploymentId, device.roleName, deployment.getChecksum() )
            isDeployed = true
        }
        // Handle race conditions with competing clients modifying device registrations, invalidating this deployment.
        catch ( arg: IllegalArgumentException ) { }
        catch ( arg: IllegalStateException ) { }

        return isDeployed
    }
}
