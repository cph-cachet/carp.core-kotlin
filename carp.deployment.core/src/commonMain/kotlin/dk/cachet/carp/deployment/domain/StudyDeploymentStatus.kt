package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import kotlinx.serialization.Serializable


/**
 * Describes the status of a [StudyDeployment]: registered devices, last received data, whether consent has been given, etc.
 */
@Serializable
sealed class StudyDeploymentStatus
{
    abstract val studyDeploymentId: UUID
    /**
     * The list of all devices part of this study deployment and their status.
     */
    abstract val devicesStatus: List<DeviceDeploymentStatus>

    /**
     * The time when the study deployment was ready for the first time (all devices deployed); null otherwise.
     */
    abstract val startTime: DateTime?


    /**
     * Initial study deployment status, indicating the invited participants have not yet acted on the invitation.
     */
    @Serializable
    data class Invited(
        override val studyDeploymentId: UUID,
        override val devicesStatus: List<DeviceDeploymentStatus>,
        override val startTime: DateTime?
    ) : StudyDeploymentStatus()

    /**
     * Participants have started registering devices, but remaining master devices still need to be deployed.
     */
    @Serializable
    data class DeployingDevices(
        override val studyDeploymentId: UUID,
        override val devicesStatus: List<DeviceDeploymentStatus>,
        override val startTime: DateTime?
    ) : StudyDeploymentStatus()

    /**
     * All master devices have been successfully deployed.
     */
    @Serializable
    data class DeploymentReady(
        override val studyDeploymentId: UUID,
        override val devicesStatus: List<DeviceDeploymentStatus>,
        override val startTime: DateTime?
    ) : StudyDeploymentStatus()

    /**
     * The study deployment has been stopped and no more data should be collected.
     */
    @Serializable
    data class Stopped(
        override val studyDeploymentId: UUID,
        override val devicesStatus: List<DeviceDeploymentStatus>,
        override val startTime: DateTime?
    ) : StudyDeploymentStatus()


    /**
     * Returns all [AnyDeviceDescriptor]'s in [devicesStatus] which still require registration.
     */
    fun getRemainingDevicesToRegister(): Set<AnyDeviceDescriptor> =
        devicesStatus.filterIsInstance<DeviceDeploymentStatus.Unregistered>().map { it.device }.toSet()

    /**
     * Returns all [AnyMasterDeviceDescriptor] which are ready for deployment and are not deployed with the correct deployment yet.
     */
    fun getRemainingDevicesReadyToDeploy(): Set<AnyMasterDeviceDescriptor> =
        devicesStatus
            .filter { it is DeviceDeploymentStatus.NotDeployed && it.canObtainDeviceDeployment }
            .map { it.device }
            .filterIsInstance<AnyMasterDeviceDescriptor>()
            .toSet()

    /**
     * Get the status of a [device] in this study deployment.
     */
    fun getDeviceStatus( device: AnyDeviceDescriptor ): DeviceDeploymentStatus =
        devicesStatus.firstOrNull { it.device == device }
            ?: throw IllegalArgumentException( "The given device was not found in this study deployment." )

    /**
     * Get the status of a device with the given [deviceRoleName] in this study deployment.
     */
    fun getDeviceStatus( deviceRoleName: String ): DeviceDeploymentStatus =
        devicesStatus.firstOrNull { it.device.roleName == deviceRoleName }
            ?: throw IllegalArgumentException( "The a device with the given role name was not found in this study deployment." )
}
