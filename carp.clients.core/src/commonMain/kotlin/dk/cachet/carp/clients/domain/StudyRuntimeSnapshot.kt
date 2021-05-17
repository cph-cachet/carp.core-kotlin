package dk.cachet.carp.clients.domain

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import kotlinx.serialization.Serializable


@Serializable
data class StudyRuntimeSnapshot(
    val studyDeploymentId: UUID,
    override val creationDate: DateTime,
    val device: AnyMasterDeviceDescriptor,
    val isDeployed: Boolean,
    val deploymentInformation: MasterDeviceDeployment?,
    val remainingDevicesToRegister: List<AnyDeviceDescriptor>,
    val isStopped: Boolean
) : Snapshot<StudyRuntime>
{
    companion object
    {
        fun fromStudyRuntime( studyRuntime: StudyRuntime ): StudyRuntimeSnapshot
        {
            val status = studyRuntime.getStatus()

            return StudyRuntimeSnapshot(
                studyRuntime.studyDeploymentId,
                studyRuntime.creationDate,
                studyRuntime.device,
                studyRuntime.isDeployed,
                (status as? StudyRuntimeStatus.DeploymentReceived)?.deploymentInformation,
                (status as? StudyRuntimeStatus.RegisteringDevices)?.remainingDevicesToRegister
                    ?: emptyList(),
                studyRuntime.isStopped
            )
        }
    }

    override fun toObject(): StudyRuntime = StudyRuntime.fromSnapshot( this )
}
