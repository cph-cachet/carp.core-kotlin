package dk.cachet.carp.client.domain

import dk.cachet.carp.client.domain.data.ConnectedDeviceDataCollector
import dk.cachet.carp.client.domain.data.DataListener
import dk.cachet.carp.client.domain.data.DeviceDataCollector
import dk.cachet.carp.client.domain.data.DeviceDataCollectorFactory
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistrationBuilder
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor


/**
 * Allows managing [StudyRuntime]'s on a client device.
 */
abstract class ClientManager<
    TMasterDevice : MasterDeviceDescriptor<TRegistration, TRegistrationBuilder>,
    TRegistration : DeviceRegistration,
    TRegistrationBuilder : DeviceRegistrationBuilder<TRegistration>
>(
    /**
     * Repository within which the state of this client is stored.
     */
    private val repository: ClientRepository,
    /**
     * The application service through which study deployments, to be run on this client, can be managed and retrieved.
     */
    private val deploymentService: DeploymentService,
    /**
     * Determines which [DeviceDataCollector] to use to collect data locally on this master device
     * and this factory is used to create [ConnectedDeviceDataCollector] instances for connected devices.
     */
    dataCollectorFactory: DeviceDataCollectorFactory
)
{
    private val dataListener: DataListener = DataListener( dataCollectorFactory )


    /**
     * Determines whether a [DeviceRegistration] has been configured for this client, which is necessary to start adding [StudyRuntime]s.
     */
    suspend fun isConfigured(): Boolean = repository.getDeviceRegistration() != null

    /**
     * Configure the [DeviceRegistration] used to register this client device in study deployments managed by the [deploymentService].
     * Use [builder] to configure device-specific registration options, if any.
     */
    suspend fun configure( builder: TRegistrationBuilder.() -> Unit = {} )
    {
        // TODO: Support reconfiguring clients, which will require reregistering the client for all study runtimes.
        require( !isConfigured() ) { "Reconfiguring clients is not supported." }

        val deviceRegistration = createDeviceRegistrationBuilder().apply( builder ).build()
        repository.setDeviceRegistration( deviceRegistration )
    }

    protected abstract fun createDeviceRegistrationBuilder(): TRegistrationBuilder

    /**
     * Get the status for the studies which run on this client device.
     */
    suspend fun getStudies(): List<StudyRuntimeStatus> = repository.getStudyRuntimeList().map { it.getStatus() }


    /**
     * Add a study which needs to be executed on this client. This involves registering this device for the specified study deployment.
     *
     * @param studyDeploymentId The ID of a study which has been deployed already and for which to collect data.
     * @param deviceRoleName The role which the client device this runtime is intended for plays as part of the deployment identified by [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     * - the client has not yet been configured
     * - a deployment with [studyDeploymentId] does not exist
     * - [deviceRoleName] is not present in the deployment or is already registered by a different device
     * - a study with the same [studyDeploymentId] and [deviceRoleName] has already been added to this client
     * - the configured device registration of this client is invalid for the specified device
     * - the configured device registration of this client uses a device ID which has already been used as part of registration of a different device
     * @return The [StudyRuntime] through which data collection for the newly added study can be managed.
     */
    suspend fun addStudy( studyDeploymentId: UUID, deviceRoleName: String ): StudyRuntimeStatus
    {
        // TODO: Can/should it be reinforced here that only study runtimes for a matching master device type can be created?
        require( isConfigured() ) { "The client has not been configured yet." }

        val alreadyAdded = repository.getStudyRuntimeBy( studyDeploymentId, deviceRoleName ) != null
        require( !alreadyAdded ) { "A study with the same study deployment ID and device role name has already been added." }

        // Create the study runtime.
        // IllegalArgumentException's will be thrown here when deployment or role name does not exist, or device is already registered.
        val deviceRegistration = repository.getDeviceRegistration()!!
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            studyDeploymentId, deviceRoleName, deviceRegistration
        )

        repository.addStudyRuntime( runtime )
        return runtime.getStatus()
    }

    /**
     * Verifies whether the device is ready for deployment of the study runtime identified by [studyRuntimeId],
     * and in case it is, deploys.
     *
     * @return True in case deployment succeeded; false in case device could not yet be deployed (e.g., awaiting registration of other devices).
     * @throws IllegalArgumentException in case no [StudyRuntime] with the given [studyRuntimeId] exists.
     * @throws UnsupportedOperationException in case deployment failed since not all necessary plugins to execute the study are available.
     */
    suspend fun tryDeployment( studyRuntimeId: StudyRuntimeId ): Boolean
    {
        val runtime = repository.getStudyRuntimeList().firstOrNull { it.id == studyRuntimeId }
        requireNotNull( runtime ) { "The specified study runtime does not exist." }

        val isDeployed = runtime.tryDeployment( deploymentService, dataListener )
        if ( isDeployed )
        {
            repository.updateStudyRuntime( runtime )
        }

        return isDeployed
    }
}
