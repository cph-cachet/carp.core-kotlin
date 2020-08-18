package dk.cachet.carp.client.domain

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.Snapshot
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptorSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StudyRuntimeSnapshot(
    val studyDeploymentId: UUID,
    override val creationDate: DateTime,
    @Serializable( DeviceDescriptorSerializer::class )
    val device: AnyMasterDeviceDescriptor,
    val isDeployed: Boolean,
    val deploymentInformation: MasterDeviceDeployment?
) : Snapshot<StudyRuntime>
{
    companion object
    {
        fun fromStudyRuntime( studyRuntime: StudyRuntime ): StudyRuntimeSnapshot
        {
            return StudyRuntimeSnapshot(
                studyRuntime.studyDeploymentId,
                studyRuntime.creationDate,
                studyRuntime.device,
                studyRuntime.isDeployed,
                studyRuntime.deploymentInformation )
        }
    }

    override fun toObject(): StudyRuntime = StudyRuntime.fromSnapshot( this )
}
