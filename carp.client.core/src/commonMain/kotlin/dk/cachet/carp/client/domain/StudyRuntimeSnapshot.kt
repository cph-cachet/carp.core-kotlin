package dk.cachet.carp.client.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


@Serializable
data class StudyRuntimeSnapshot(
    @Serializable( UUIDSerializer::class )
    val studyDeploymentId: UUID,
    @Serializable( DeviceDescriptorSerializer::class )
    val device: AnyMasterDeviceDescriptor,
    val isDeployed: Boolean )
{
    companion object
    {
        fun fromStudyRuntime( studyRuntime: StudyRuntime ): StudyRuntimeSnapshot
        {
            return StudyRuntimeSnapshot( studyRuntime.studyDeploymentId, studyRuntime.device, studyRuntime.isDeployed )
        }
    }
}