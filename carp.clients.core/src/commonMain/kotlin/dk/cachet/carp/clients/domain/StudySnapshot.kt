package dk.cachet.carp.clients.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
data class StudySnapshot(
    val studyDeploymentId: UUID,
    val deviceRoleName: String,
    override val createdOn: Instant,
    val isDeployed: Boolean,
    val deploymentInformation: MasterDeviceDeployment?,
    val remainingDevicesToRegister: Set<AnyDeviceDescriptor> = emptySet(),
    val isStopped: Boolean
) : Snapshot<Study>
{
    companion object
    {
        fun fromStudy( study: Study ): StudySnapshot
        {
            val status = study.getStatus()

            return StudySnapshot(
                study.studyDeploymentId,
                study.deviceRoleName,
                study.createdOn,
                study.isDeployed,
                (status as? StudyStatus.DeploymentReceived)?.deploymentInformation,
                (status as? StudyStatus.RegisteringDevices)?.remainingDevicesToRegister?.toSet()
                    ?: emptySet(),
                study.isStopped
            )
        }
    }

    override fun toObject(): Study = Study.fromSnapshot( this )
}
