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
     * Returns all [AnyDeviceDescriptor]'s in [devicesStatus] which require registration.
     */
    fun getRemainingDevicesToRegister(): Set<AnyDeviceDescriptor> =
        devicesStatus.filter { it.requiresRegistration && it is DeviceDeploymentStatus.Unregistered }.map { it.device }.toSet()

    /**
     * Returns all [AnyMasterDeviceDescriptor] which are ready for deployment and have not yet been deployed.
     */
    fun getRemainingDevicesReadyToDeploy(): Set<AnyMasterDeviceDescriptor> =
        devicesStatus
            .filter { it is DeviceDeploymentStatus.NotDeployed && it.isReadyForDeployment }
            .map { it.device }
            .filterIsInstance<AnyMasterDeviceDescriptor>()
            .toSet()
}
