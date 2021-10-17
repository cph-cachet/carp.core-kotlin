package dk.cachet.carp.clients.domain

import dk.cachet.carp.clients.domain.data.DataListener
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.deployments.application.MasterDeviceDeployment


/**
 * A study deployment, identified by [studyDeploymentId], a client device participates with the role [deviceRoleName].
 */
class Study(
    /**
     * The ID of the deployed study for which to collect data.
     */
    val studyDeploymentId: UUID,
    /**
     * The role name of the device this runtime is intended for within the deployment identified by [studyDeploymentId].
     */
    val deviceRoleName: String
) : AggregateRoot<Study, StudySnapshot, Study.Event>()
{
    sealed class Event : DomainEvent()
    {
        data class DeploymentReceived(
            val deploymentInformation: MasterDeviceDeployment,
            val remainingDevicesToRegister: Set<AnyDeviceDescriptor>
        ) : Event()

        object DeploymentCompleted : Event()

        object DeploymentStopped : Event()
    }


    companion object Factory
    {
        internal fun fromSnapshot( snapshot: StudySnapshot ): Study =
            Study( snapshot.studyDeploymentId, snapshot.deviceRoleName ).apply {
                createdOn = snapshot.createdOn
                isDeployed = snapshot.isDeployed
                deploymentInformation = snapshot.deploymentInformation
                remainingDevicesToRegister = snapshot.remainingDevicesToRegister.toSet()
                isStopped = snapshot.isStopped
            }
    }


    /**
     * Composite ID for this study, comprised of the [studyDeploymentId] and [deviceRoleName].
     */
    val id: StudyId get() = StudyId( studyDeploymentId, deviceRoleName )

    /**
     * Determines whether the device deployment has completed successfully.
     */
    var isDeployed: Boolean = false
        private set

    private var remainingDevicesToRegister: Set<AnyDeviceDescriptor> = emptySet()
    private var deploymentInformation: MasterDeviceDeployment? = null

    /**
     * Determines whether the study has stopped and no further data is being collected.
     */
    var isStopped: Boolean = false
        private set


    /**
     * Get the status of this [Study].
     */
    fun getStatus(): StudyStatus =
        when {
            deploymentInformation == null -> StudyStatus.NotReadyForDeployment( id )
            remainingDevicesToRegister.isNotEmpty() ->
                StudyStatus.RegisteringDevices( id, deploymentInformation!!, remainingDevicesToRegister.toSet() )
            isStopped -> StudyStatus.Stopped( id, deploymentInformation!! )
            isDeployed -> StudyStatus.Deployed( id, deploymentInformation!! )
            else -> error( "Unexpected study state." )
        }

    /**
     * A new master device [deployment] determining what data to collect for this study has been received.
     * The [remainingDevicesToRegister] need to be registered before deployment can be completed.
     *
     * @throws IllegalArgumentException when the role name [deployment] is intended for is different from the expected [deviceRoleName].
     */
    fun deploymentReceived( deployment: MasterDeviceDeployment, remainingDevicesToRegister: Set<AnyDeviceDescriptor> )
    {
        require( deployment.deviceDescriptor.roleName == deviceRoleName )
            { "The deployment is intended for a device with a different role name." }

        deploymentInformation = deployment
        this.remainingDevicesToRegister = remainingDevicesToRegister.toSet()

        event( Event.DeploymentReceived( deployment, remainingDevicesToRegister.toSet() ) )
    }

    /**
     * Complete the deployment if all prerequisites are met, or throw an exception otherwise.
     *
     * @throws IllegalStateException when:
     *  - deployment hasn't been received yet
     *  - not all required devices have been registered
     * @throws UnsupportedOperationException in case not all necessary plugins to execute the deployment are available.
     */
    fun completeDeployment( dataListener: DataListener )
    {
        val deployment = checkNotNull( deploymentInformation )

        // All devices need to be registered before deployment can be validated.
        check( remainingDevicesToRegister.isEmpty() )

        // Verify whether data collection is supported on all connected devices and for all requested measures.
        for ( device in deployment.getRuntimeDeviceInfo() )
        {
            // It shouldn't be possible to be ready for deployment when connected device registration is not set.
            val registration = checkNotNull( device.registration )

            // Verify whether connected device is supported.
            val deviceType = device.descriptor::class
            if ( device.isConnectedDevice )
            {
                dataListener.tryGetConnectedDataCollector( deviceType, registration )
                    ?: throw UnsupportedOperationException( "Connecting to device of type \"$deviceType\" is not supported on this client." )
            }

            val dataTypes = device.tasks
                .flatMap { it.measures.filterIsInstance<Measure.DataStream>() }
                .map { it.type }
                .distinct()
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

        isDeployed = true
        event( Event.DeploymentCompleted )
    }

    /**
     * Permanently stop collecting data for this [Study].
     */
    fun stop(): StudyStatus
    {
        // Early out in case study has already been stopped.
        val status = getStatus()
        if ( status is StudyStatus.Stopped ) return status

        isStopped = true
        event( Event.DeploymentStopped )

        return getStatus()
    }

    /**
     * Get a serializable snapshot of the current state of this [Study].
     */
    override fun getSnapshot(): StudySnapshot = StudySnapshot.fromStudy( this )
}
