package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import kotlinx.serialization.Serializable


/**
 * Describes the status of a [StudyDeployment]: registered devices, last received data, whether consent has been given, etc.
 */
@Serializable
data class StudyDeploymentStatus(
    val studyDeploymentId: UUID,
    /**
     * The list of all devices part of this study deployment and their status.
     */
    val devicesStatus: List<DeviceDeploymentStatus>
)
{
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
            .filter { it is DeviceDeploymentStatus.NotDeployed && it.isReadyForDeployment }
            .map { it.device }
            .filterIsInstance<AnyMasterDeviceDescriptor>()
            .toSet()

    /**
     * Get the status of a [device] in this study deployment.
     */
    fun getDeviceStatus( device: AnyDeviceDescriptor ): DeviceDeploymentStatus =
        devicesStatus.firstOrNull { it.device == device }
            ?: throw IllegalArgumentException( "The given device was not found in this study deployment." )
}
