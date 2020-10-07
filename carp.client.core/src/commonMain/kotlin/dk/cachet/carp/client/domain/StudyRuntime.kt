package dk.cachet.carp.client.domain

import dk.cachet.carp.client.domain.data.DataCollector
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.common.ddd.AggregateRoot
import dk.cachet.carp.common.ddd.DomainEvent
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.domain.DeviceDeploymentStatus
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Manage data collection for a particular study on a client device.
 */
class StudyRuntime private constructor(
    /**
     * The ID of the deployed study for which to collect data.
     */
    val studyDeploymentId: UUID,
    /**
     * The description of the device this runtime is intended for within the deployment identified by [studyDeploymentId].
     */
    val device: AnyMasterDeviceDescriptor
) : AggregateRoot<StudyRuntime, StudyRuntimeSnapshot, StudyRuntime.Event>(), StudyRuntimeStatus
{
    sealed class Event : DomainEvent()
    {
        data class Deployed( val deploymentInformation: MasterDeviceDeployment ) : Event()
    }


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
         * @throws IllegalStateException in case data requested in the deployment cannot be collected on this client.
         */
        internal suspend fun initialize(
            /**
             * The application service to use to retrieve and manage the study deployment with [studyDeploymentId].
             * This deployment service should have the deployment with [studyDeploymentId] available.
             */
            deploymentService: DeploymentService,
            /**
             * Manages [Data] collection of requested [DataType]s for this master device and connected devices.
             */
            dataCollector: DataCollector,
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
            val clientDeviceStatus = deploymentStatus.getDeviceStatus( deviceRoleName )
            val runtime = StudyRuntime( studyDeploymentId, clientDeviceStatus.device as AnyMasterDeviceDescriptor )

            // After registration, deployment information might immediately be available for this client device.
            runtime.tryDeployment( deploymentService, dataCollector, deploymentStatus )

            return runtime
        }

        internal fun fromSnapshot( snapshot: StudyRuntimeSnapshot ): StudyRuntime =
            StudyRuntime( snapshot.studyDeploymentId, snapshot.device ).apply {
                creationDate = snapshot.creationDate
                isDeployed = snapshot.isDeployed
                deploymentInformation = snapshot.deploymentInformation
            }
    }


    /**
     * Composite ID for this study runtime, comprised of the [studyDeploymentId] and [device] role name.
     */
    override val id: StudyRuntimeId get() = StudyRuntimeId( studyDeploymentId, device.roleName )

    /**
     * Determines whether the device has retrieved its [MasterDeviceDeployment] and was able to load all the necessary plugins to execute the study.
     */
    override var isDeployed: Boolean = false
        private set

    /**
     * In case deployment succeeded ([isDeployed] is true), this contains all the information on the study to run.
     * TODO: This should be consumed within this domain model and not be public.
     *       Currently, it is in order to work towards a first MVP which includes server/client communication through the domain model.
     */
    override var deploymentInformation: MasterDeviceDeployment? = null
        private set

    /**
     * Verifies whether the device is ready for deployment and in case it is, deploys.
     *
     * @return True in case deployment succeeded; false in case device could not yet be deployed (e.g., awaiting registration of other devices).
     * @throws UnsupportedOperationException in case deployment failed since not all necessary plugins to execute the study are available.
     * @throws IllegalStateException in case data requested in the deployment cannot be collected on this client.
     */
    suspend fun tryDeployment( deploymentService: DeploymentService, dataCollector: DataCollector ): Boolean
    {
        val deploymentStatus = deploymentService.getStudyDeploymentStatus( studyDeploymentId )

        return tryDeployment( deploymentService, dataCollector, deploymentStatus )
    }

    private suspend fun tryDeployment(
        deploymentService: DeploymentService,
        dataCollector: DataCollector,
        deploymentStatus: StudyDeploymentStatus
    ): Boolean
    {
        // Early out in case state indicates the device is not yet ready to deploy.
        val deviceStatus = deploymentStatus.getDeviceStatus( device )
        if ( deviceStatus !is DeviceDeploymentStatus.NotDeployed || !deviceStatus.isReadyForDeployment ) return false

        // Get deployment information.
        val deployment = deploymentService.getDeviceDeploymentFor( studyDeploymentId, device.roleName )
        check( deployment.deviceDescriptor == device )
        deploymentInformation = deployment

        // Verify whether data  collection is supported for all requested measures.
        for ( deviceTasks in deployment.getTasksPerDevice() )
        {
            val dataTypes = deviceTasks.tasks.flatMap { it.measures }.map { it.type }.distinct()
            for ( type in dataTypes )
            {
                val canCollectData =
                    if ( deviceTasks.isConnectedDevice )
                    {
                        dataCollector.supportsDataCollectionOnConnectedDevice(
                            type,
                            deviceTasks.device::class,
                            deviceTasks.deviceRegistration )
                    }
                    else dataCollector.supportsDataCollection( type )

                check( canCollectData )
                    { "Data collection for data type \"$type\" on device with role \"${device.roleName}\" is not supported on this client." }
            }
        }

        // Notify deployment service of successful deployment.
        try
        {
            deploymentService.deploymentSuccessful( studyDeploymentId, device.roleName, deployment.lastUpdateDate )
            isDeployed = true
            event( Event.Deployed( deployment ) )
        }
        // Handle race conditions with competing clients modifying device registrations, invalidating this deployment.
        catch ( ignore: IllegalArgumentException ) { }
        catch ( ignore: IllegalStateException ) { }

        return isDeployed
    }

    /**
     * Get a serializable snapshot of the current state of this [StudyRuntime].
     */
    override fun getSnapshot(): StudyRuntimeSnapshot = StudyRuntimeSnapshot.fromStudyRuntime( this )
}
