package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentManager
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


/**
 * Manage data collection for a particular study on a client device.
 */
class StudyRuntime private constructor(
    private val deploymentManager: DeploymentManager,
    /**
     * The ID of the deployed study for which to collect data.
     */
    val studyDeploymentId: UUID,
    /**
     * The description of the device this runtime is intended for within the deployment identified by [studyDeploymentId].
     */
    val device: AnyMasterDeviceDescriptor )
{
    /**
     * Determines whether a study runtime is ready for deployment, and once it is, whether it has been deployed successfully.
     */
    @Serializable
    data class DeploymentState(
        /**
         * True if all dependent devices have been registered and this device is ready for deployment.
         */
        val isReadyForDeployment: Boolean,
        /**
         * True if the device has retrieved its [MasterDeviceDeployment] and was able to load all the necessary plugins to execute the study.
         */
        val isDeployed: Boolean )


    companion object Factory
    {
        /**
         * Instantiate a [StudyRuntime] by registering the client device in the [deploymentManager].
         * In case the device is immediately ready for deployment, also deploy.
         *
         * @throws UnsupportedOperationException in case deployment failed since not all necessary plugins to execute the study are available.
         */
        internal fun initialize(
            /**
             * The application service to use to retrieve and manage the study deployment with [studyDeploymentId].
             * This deployment manager should have the deployment with [studyDeploymentId] available.
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
            // TODO: What if registration succeeds, but deployment fails since not all required plugins are available? Registering again is not allowed right now.
            val deploymentStatus = deploymentManager.registerDevice( studyDeploymentId, deviceRoleName, deviceRegistration )

            // Initialize runtime.
            val clientDeviceStatus = deploymentStatus.devicesStatus.first { it.device.roleName == deviceRoleName }
            val runtime = StudyRuntime( deploymentManager, studyDeploymentId, clientDeviceStatus.device as AnyMasterDeviceDescriptor )

            // After registration, deployment information might immediately be available for this client device.
            if ( clientDeviceStatus.isReadyForDeployment )
            {
                runtime.deploymentInformation = deploymentManager.getDeviceDeploymentFor( studyDeploymentId, deviceRoleName )
                runtime.deploy( runtime.deploymentInformation!! )
            }

            return runtime
        }

        internal fun fromSnapshot( snapshot: StudyRuntimeSnapshot, deploymentManager: DeploymentManager ): StudyRuntime
        {
            val runtime = StudyRuntime( deploymentManager, snapshot.studyDeploymentId, snapshot.device )
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
     * @throws UnsupportedOperationException in case deployment failed since not all necessary plugins to execute the study are available.
     */
    fun tryDeployment(): DeploymentState
    {
        val deploymentStatus = deploymentManager.getStudyDeploymentStatus( studyDeploymentId )
        val clientDeviceStatus = deploymentStatus.devicesStatus.first { it.device == device }

        if ( clientDeviceStatus.isReadyForDeployment )
        {
            deploymentInformation = deploymentManager.getDeviceDeploymentFor( studyDeploymentId, device.roleName )
            deploy( deploymentInformation!! )
        }

        return DeploymentState( clientDeviceStatus.isReadyForDeployment, isDeployed )
    }

    private fun deploy( deploymentInformation: MasterDeviceDeployment )
    {
        // TODO: Throw exception in case there are missing plugins to perform the operations (e.g., measurements) specified in the deployment.

        isDeployed = true
    }
}