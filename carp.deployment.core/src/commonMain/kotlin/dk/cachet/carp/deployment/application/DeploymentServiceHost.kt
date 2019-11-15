package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.*
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*


/**
 * Implementation of [DeploymentService] which allows deploying [StudyProtocol]'s and retrieving [MasterDeviceDeployment]'s for participating master devices as defined in the protocol.
 */
class DeploymentServiceHost( private val repository: DeploymentRepository ) : DeploymentService
{
    /**
     * Instantiate a study deployment for a given [StudyProtocolSnapshot].
     *
     * @throws InvalidConfigurationError when [protocol] is invalid.
     * @return The [StudyDeploymentStatus] of the newly created study deployment.
     */
    override suspend fun createStudyDeployment( protocol: StudyProtocolSnapshot ): StudyDeploymentStatus
    {
        val newDeployment = StudyDeployment( protocol )

        repository.add( newDeployment )

        return newDeployment.getStatus()
    }

    /**
     * Get the status for a study deployment with the given [studyDeploymentId].
     *
     * @param studyDeploymentId The id of the [StudyDeployment] to return [StudyDeploymentStatus] for.
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist.
     */
    override suspend fun getStudyDeploymentStatus( studyDeploymentId: UUID ): StudyDeploymentStatus
    {
        val deployment: StudyDeployment = repository.getStudyDeploymentBy( studyDeploymentId )

        return deployment.getStatus()
    }

    /**
     * Register the device with the specified [deviceRoleName] for the study deployment with [studyDeploymentId].
     *
     * @param studyDeploymentId The id of the [StudyDeployment] to register the device for.
     * @param deviceRoleName The role name of the device in the deployment to register.
     * @param registration A matching configuration for the device with [deviceRoleName].
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist,
     * [deviceRoleName] is not present in the deployment or is already registered,
     * or [registration] is invalid for the specified device or uses a device ID which has already been used as part of registration of a different device.
     */
    override suspend fun registerDevice( studyDeploymentId: UUID, deviceRoleName: String, registration: DeviceRegistration ): StudyDeploymentStatus
    {
        val deployment = repository.getStudyDeploymentBy( studyDeploymentId )

        val device = deployment.registrableDevices.firstOrNull { it.device.roleName == deviceRoleName }
            ?: throw IllegalArgumentException( "The specified device role name could not be found in the study deployment." )
        deployment.registerDevice( device.device, registration )

        repository.update( deployment )

        return deployment.getStatus()
    }

    /**
     * Get the deployment configuration for the master device with [masterDeviceRoleName] in the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist,
     * or [masterDeviceRoleName] is not present in the deployment, or not yet registered.
     */
    override suspend fun getDeviceDeploymentFor( studyDeploymentId: UUID, masterDeviceRoleName: String ): MasterDeviceDeployment
    {
        val deployment = repository.getStudyDeploymentBy( studyDeploymentId )

        val device = deployment.registeredDevices.keys.firstOrNull { it.roleName == masterDeviceRoleName }
            ?: throw IllegalArgumentException( "The specified device role name is not part of this study deployment or is not yet registered." )
        val masterDevice = device as? MasterDeviceDescriptor<*,*>
            ?:  throw IllegalArgumentException( "The specified device is not a master device and therefore does not have a deployment configuration." )

        return deployment.getDeviceDeploymentFor( masterDevice )
    }
}