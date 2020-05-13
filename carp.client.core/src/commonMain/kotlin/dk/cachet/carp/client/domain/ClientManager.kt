package dk.cachet.carp.client.domain

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
    private val deploymentService: DeploymentService
)
{
    /**
     * Determines whether a [DeviceRegistration] has been configured for this client, which is necessary to start adding [StudyRuntime]s.
     */
    val isConfigured: Boolean get() = repository.deviceRegistration != null

    /**
     * Configure the [DeviceRegistration] used to register this client device in study deployments managed by the [deploymentService].
     * Use [builder] to configure device-specific registration options, if any.
     */
    fun configure( builder: TRegistrationBuilder.() -> Unit = {} )
    {
        // TODO: Support reconfiguring clients, which will require reregistering the client for all study runtimes.
        require( !isConfigured ) { "Reconfiguring clients is not supported." }

        val deviceRegistration = createDeviceRegistrationBuilder().apply( builder ).build()
        repository.deviceRegistration = deviceRegistration
    }

    protected abstract fun createDeviceRegistrationBuilder(): TRegistrationBuilder

    /**
     * The studies which run on this client device.
     */
    val studies: List<StudyRuntime> get() = repository.getStudyRuntimeList()


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
    suspend fun addStudy( studyDeploymentId: UUID, deviceRoleName: String ): StudyRuntime
    {
        // TODO: Can/should it be reinforced here that only study runtimes for a matching master device type can be created?
        require( isConfigured ) { "The client has not been configured yet." }

        val alreadyAdded = repository.getStudyRuntimeBy( studyDeploymentId, deviceRoleName ) != null
        require( !alreadyAdded ) { "A study with the same study deployment ID and device role name has already been added." }

        // Create the study runtime.
        // IllegalArgumentException's will be thrown here when deployment or role name does not exist, or device is already registered.
        val deviceRegistration = repository.deviceRegistration!!
        val runtime = StudyRuntime.initialize( deploymentService, studyDeploymentId, deviceRoleName, deviceRegistration )

        repository.addStudyRuntime( runtime )
        return runtime
    }
}
