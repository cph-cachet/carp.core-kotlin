package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptorSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StudyRuntimeSnapshot(
    val studyDeploymentId: UUID,
    @Serializable( DeviceDescriptorSerializer::class )
    val device: AnyMasterDeviceDescriptor,
    val isDeployed: Boolean,
    val deploymentInformation: MasterDeviceDeployment?
)
{
    companion object
    {
        fun fromStudyRuntime( studyRuntime: StudyRuntime ): StudyRuntimeSnapshot
        {
            return StudyRuntimeSnapshot( studyRuntime.studyDeploymentId, studyRuntime.device, studyRuntime.isDeployed, studyRuntime.deploymentInformation )
        }
    }
}
