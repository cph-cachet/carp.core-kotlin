package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * Describes the status of a study deployment: registered devices, last received data, whether consent has been given, etc.
 */
@Serializable
sealed class StudyDeploymentStatus
{
    /**
     * The time when the deployment was created.
     */
    abstract val createdOn: Instant

    abstract val studyDeploymentId: UUID
    /**
     * The list of all devices part of this study deployment and their status.
     */
    abstract val devicesStatus: List<DeviceDeploymentStatus>

    /**
     * The time when the study deployment was ready for the first time (all devices deployed); null otherwise.
     */
    abstract val startedOn: Instant?


    /**
     * Initial study deployment status, indicating the invited participants have not yet acted on the invitation.
     */
    @Serializable
    data class Invited(
        override val createdOn: Instant,
        override val studyDeploymentId: UUID,
        override val devicesStatus: List<DeviceDeploymentStatus>,
        override val startedOn: Instant?
    ) : StudyDeploymentStatus()

    /**
     * Participants have started registering devices, but remaining master devices still need to be deployed.
     */
    @Serializable
    data class DeployingDevices(
        override val createdOn: Instant,
        override val studyDeploymentId: UUID,
        override val devicesStatus: List<DeviceDeploymentStatus>,
        override val startedOn: Instant?
    ) : StudyDeploymentStatus()

    /**
     * All master devices have been successfully deployed.
     */
    @Serializable
    data class DeploymentReady(
        override val createdOn: Instant,
        override val studyDeploymentId: UUID,
        override val devicesStatus: List<DeviceDeploymentStatus>,
        override val startedOn: Instant?
    ) : StudyDeploymentStatus()

    /**
     * The study deployment has been stopped and no more data should be collected.
     */
    @Serializable
    data class Stopped(
        override val createdOn: Instant,
        override val studyDeploymentId: UUID,
        override val devicesStatus: List<DeviceDeploymentStatus>,
        override val startedOn: Instant?,
        val stoppedOn: Instant
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
            ?: throw IllegalArgumentException( "The device with the given role name was not found in this study deployment." )
}
