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
            // TODO: What if registration succeeds, but deployment fails since not all required plugins are available? Registering again is not allowed right now.
            val deploymentStatus = deploymentService.registerDevice( studyDeploymentId, deviceRoleName, deviceRegistration )

            // Initialize runtime.
            val clientDeviceStatus = deploymentStatus.devicesStatus.first { it.device.roleName == deviceRoleName }
            val runtime = StudyRuntime( deploymentService, studyDeploymentId, clientDeviceStatus.device as AnyMasterDeviceDescriptor )

            // After registration, deployment information might immediately be available for this client device.
            if ( clientDeviceStatus is DeviceDeploymentStatus.NotDeployed && clientDeviceStatus.isReadyForDeployment )
            {
                runtime.deploymentInformation = deploymentService.getDeviceDeploymentFor( studyDeploymentId, deviceRoleName )
                runtime.deploy( runtime.deploymentInformation!! )
            }

            return runtime
        }

        internal fun fromSnapshot( snapshot: StudyRuntimeSnapshot, deploymentService: DeploymentService ): StudyRuntime
        {
            val runtime = StudyRuntime( deploymentService, snapshot.studyDeploymentId, snapshot.device )
            runtime.isDeployed = snapshot.isDeployed
            runtime.deploymentInformation = snapshot.deploymentInformation

            return runtime
        }
    }


    /**
     * Determines whether the device has retrieved its [MasterDeviceDeployment] and was able to load all the necessary plugins to execute the study.
     */
    var isDeployed: Boolean = false
        private set

    /**
     * In case deployment succeeded ([isDeployed] is true), this contains all the information on the study the run.
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
        val clientDeviceStatus = deploymentStatus.devicesStatus.first { it.device == device }

        if ( clientDeviceStatus is DeviceDeploymentStatus.NotDeployed && clientDeviceStatus.isReadyForDeployment )
        {
            deploymentInformation = deploymentService.getDeviceDeploymentFor( studyDeploymentId, device.roleName )
            deploy( deploymentInformation!! )
        }

        return isDeployed
    }

    private fun deploy( deploymentInformation: MasterDeviceDeployment )
    {
        // TODO: Throw exception in case there are missing plugins to perform the operations (e.g., measurements) specified in the deployment.

        isDeployed = true
    }
}
