package dk.cachet.carp.client.domain

import dk.cachet.carp.client.domain.data.DataListener
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.common.ddd.AggregateRoot
import dk.cachet.carp.common.ddd.DomainEvent
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.domain.DeviceDeploymentStatus
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
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
) : AggregateRoot<StudyRuntime, StudyRuntimeSnapshot, StudyRuntime.Event>()
{
    sealed class Event : DomainEvent()
    {
        data class DeploymentReceived(
            val deploymentInformation: MasterDeviceDeployment,
            val remainingDevicesToRegister: List<AnyDeviceDescriptor>
        ) : Event()

        object DeploymentCompleted : Event()

        object DeploymentStopped : Event()
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
             * Allows subscribing to [Data] of requested [DataType]s for this master device and connected devices.
             */
            dataListener: DataListener,
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
            runtime.tryDeployment( deploymentService, dataListener, deploymentStatus )

            return runtime
        }

        internal fun fromSnapshot( snapshot: StudyRuntimeSnapshot ): StudyRuntime =
            StudyRuntime( snapshot.studyDeploymentId, snapshot.device ).apply {
                creationDate = snapshot.creationDate
                isDeployed = snapshot.isDeployed
                deploymentInformation = snapshot.deploymentInformation
                remainingDevicesToRegister = snapshot.remainingDevicesToRegister
            }
    }


    /**
     * Composite ID for this study runtime, comprised of the [studyDeploymentId] and [device] role name.
     */
    val id: StudyRuntimeId get() = StudyRuntimeId( studyDeploymentId, device.roleName )

    /**
     * Determines whether the device deployment has completed successfully.
     */
    var isDeployed: Boolean = false
        private set

    private var remainingDevicesToRegister: List<AnyDeviceDescriptor> = emptyList()
    private var deploymentInformation: MasterDeviceDeployment? = null

    /**
     * Determines whether the study has stopped and no more further data is being collected.
     */
    var isStopped: Boolean = false
        private set


    /**
     * Get the status of this [StudyRuntime].
     */
    fun getStatus(): StudyRuntimeStatus =
        when {
            deploymentInformation == null -> StudyRuntimeStatus.NotReadyForDeployment( id )
            remainingDevicesToRegister.isNotEmpty() ->
                StudyRuntimeStatus.RegisteringDevices( id, deploymentInformation!!, remainingDevicesToRegister )
            isStopped -> StudyRuntimeStatus.Stopped( id, deploymentInformation!! )
            isDeployed -> StudyRuntimeStatus.Deployed( id, deploymentInformation!! )
            else -> error( "Unexpected study runtime state." )
        }

    /**
     * Verifies whether the device is ready for deployment and in case it is, deploys.
     * In case already deployed, nothing happens.
     *
     * @throws UnsupportedOperationException in case deployment failed since not all necessary plugins to execute the study are available.
     */
    suspend fun tryDeployment( deploymentService: DeploymentService, dataListener: DataListener ): StudyRuntimeStatus
    {
        val deploymentStatus = deploymentService.getStudyDeploymentStatus( studyDeploymentId )

        tryDeployment( deploymentService, dataListener, deploymentStatus )
        return getStatus()
    }

    // TODO: Handle `NeedsRedeployment`, invalidating the retrieved deployment information.
    private suspend fun tryDeployment(
        deploymentService: DeploymentService,
        dataListener: DataListener,
        deploymentStatus: StudyDeploymentStatus
    )
    {
        // Early out in case state indicates the device is already deployed or deployment cannot yet be obtained.
        val deviceStatus = deploymentStatus.getDeviceStatus( device )
        if ( deviceStatus !is DeviceDeploymentStatus.NotDeployed ) return
        if ( !deviceStatus.canObtainDeviceDeployment ) return

        // Get deployment information.
        // TODO: Handle race condition in case other devices were unregistered in between.
        val deployment = deploymentService.getDeviceDeploymentFor( studyDeploymentId, device.roleName )
        check( deployment.deviceDescriptor == device )
        deploymentInformation = deployment
        remainingDevicesToRegister = deploymentStatus.devicesStatus
            .map { it.device }
            .filter { it.roleName in deviceStatus.remainingDevicesToRegisterBeforeDeployment }
        event( Event.DeploymentReceived( deployment, remainingDevicesToRegister.toList() ) )

        // Early out in case devices need to be registered before being able to complete deployment.
        if ( remainingDevicesToRegister.isNotEmpty() ) return

        // Verify whether data collection is supported on all connected devices and for all requested measures.
        for ( (device, tasks) in deployment.getTasksPerDevice() )
        {
            // It shouldn't be possible to be ready for deployment when connected device registration is not set.
            val registration = device.registration
            checkNotNull( registration )

            // Verify whether connected device is supported.
            val deviceType = device.descriptor::class
            if ( device.isConnectedDevice )
            {
                dataListener.tryGetConnectedDataCollector( deviceType, registration )
                    ?: throw UnsupportedOperationException( "Connecting to device of type \"$deviceType\" is not supported on this client." )
            }

            val dataTypes = tasks.flatMap { it.measures }.map { it.type }.distinct()
            for ( dataType in dataTypes )
            {
                val supportsData =
                    if ( device.isConnectedDevice )
                    {
                        dataListener.supportsDataOnConnectedDevice( dataType, deviceType, registration )
                    }
                    else dataListener.supportsData( dataType )

                if ( !supportsData )
                {
                    throw UnsupportedOperationException(
                        "Subscribing to data of data type \"$dataType\" " +
                        "on device with role \"${device.descriptor.roleName}\" is not supported on this client."
                    )
                }
            }
        }

        // Notify deployment service of successful deployment.
        try
        {
            deploymentService.deploymentSuccessful( studyDeploymentId, device.roleName, deployment.lastUpdateDate )
            isDeployed = true
            event( Event.DeploymentCompleted )
        }
        // Handle race conditions with competing clients modifying device registrations, invalidating this deployment.
        catch ( ignore: IllegalArgumentException ) { } // TODO: When deployment is out of date, maybe also use `IllegalStateException` for easier handling here.
        catch ( ignore: IllegalStateException ) { }
    }

    /**
     * Permanently stop collecting data for this [StudyRuntime].
     */
    suspend fun stop( deploymentService: DeploymentService ): StudyRuntimeStatus
    {
        // Early out in case study has already been stopped.
        val status = getStatus()
        if ( status is StudyRuntimeStatus.Stopped ) return status

        // Stop study deployment.
        // TODO: Right now this requires the client to be online in case `deploymentService` is an online service.
        //       Once we have domain events in place this should be modeled as a request to stop deployment which is cached when offline.
        val deploymentStatus = deploymentService.stop( studyDeploymentId )
        check( deploymentStatus is StudyDeploymentStatus.Stopped )
        isStopped = true
        event( Event.DeploymentStopped )

        return getStatus()
    }

    /**
     * Get a serializable snapshot of the current state of this [StudyRuntime].
     */
    override fun getSnapshot(): StudyRuntimeSnapshot = StudyRuntimeSnapshot.fromStudyRuntime( this )
}
