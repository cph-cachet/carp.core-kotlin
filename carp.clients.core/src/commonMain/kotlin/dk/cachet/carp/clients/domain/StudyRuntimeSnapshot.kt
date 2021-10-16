package dk.cachet.carp.clients.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
data class StudyRuntimeSnapshot(
    val studyDeploymentId: UUID,
    val deviceRoleName: String,
    override val createdOn: Instant,
    val isDeployed: Boolean,
    val deploymentInformation: MasterDeviceDeployment?,
    val remainingDevicesToRegister: Set<AnyDeviceDescriptor> = emptySet(),
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
                studyRuntime.deviceRoleName,
                studyRuntime.createdOn,
                studyRuntime.isDeployed,
                (status as? StudyRuntimeStatus.DeploymentReceived)?.deploymentInformation,
                (status as? StudyRuntimeStatus.RegisteringDevices)?.remainingDevicesToRegister?.toSet()
                    ?: emptySet(),
                studyRuntime.isStopped
            )
        }
    }

    override fun toObject(): StudyRuntime = StudyRuntime.fromSnapshot( this )
}
