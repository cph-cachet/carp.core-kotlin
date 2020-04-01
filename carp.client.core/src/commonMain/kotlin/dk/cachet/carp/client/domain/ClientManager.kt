package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor


/**
 * Allows managing [StudyRuntime]'s on a client device.
 */
class ClientManager<TMasterDevice : MasterDeviceDescriptor<TRegistration, *>, TRegistration : DeviceRegistration>(
    /**
     * The device configuration for this client device, used to register for study deployments managed by the [deploymentService].
     */
    val deviceRegistration: TRegistration,
    /**
     * The application service through which study deployments can be managed and retrieved.
     */
    private val deploymentService: DeploymentService
)
{
    companion object Factory
    {
        fun fromSnapshot( snapshot: ClientManagerSnapshot, deploymentService: DeploymentService ): ClientManager<*, *>
        {
            val manager = ClientManager( snapshot.deviceRegistration, deploymentService )

            // Add running studies.
            snapshot.studies.forEach {
                val study = StudyRuntime.fromSnapshot( it, deploymentService )
                manager._studies.add( study ) }

            return manager
        }
    }


    private val _studies: MutableList<StudyRuntime> = mutableListOf()
    /**
     * The studies which run on this client device.
     */
    val studies: List<StudyRuntime> = _studies


    /**
     * Add a study which needs to be executed on this client. This involves registering this device for the specified study deployment.
     *
     * @param studyDeploymentId The ID of a study which has been deployed already and for which to collect data.
     * @param deviceRoleName The role which the client device this runtime is intended for plays as part of the deployment identified by [studyDeploymentId].
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist,
     * [deviceRoleName] is not present in the deployment or is already registered by a different device,
     * a study with the same [studyDeploymentId] and [deviceRoleName] has already been added to this client,
     * or the [deviceRegistration] of this client is invalid for the specified device or uses a device ID which has already been used as part of registration of a different device.
     * @return The [StudyRuntime] through which data collection for the newly added study can be managed.
     */
    suspend fun addStudy( studyDeploymentId: UUID, deviceRoleName: String ): StudyRuntime
    {
        // TODO: Can/should it be reinforced here that only study runtimes for a matching master device type can be created?

        val alreadyAdded = studies.any { it.studyDeploymentId == studyDeploymentId && it.device.roleName == deviceRoleName }
        require( !alreadyAdded ) { "A study with the same study deployment ID and device role name has already been added." }

        // Create the study runtime.
        // IllegalArgumentException's will be thrown here when deployment or role name does not exist, or device is already registered.
        val runtime = StudyRuntime.initialize( deploymentService, studyDeploymentId, deviceRoleName, deviceRegistration )

        _studies.add( runtime )
        return runtime
    }

    /**
     * Get a serializable snapshot of the current state of this [ClientManager].
     */
    fun getSnapshot(): ClientManagerSnapshot
    {
        return ClientManagerSnapshot.fromClientManager( this )
    }
}
