package dk.cachet.carp.clients.domain.study

import dk.cachet.carp.clients.application.study.StudyStatus
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
data class StudySnapshot(
    val studyDeploymentId: UUID,
    val deviceRoleName: String,
    override val createdOn: Instant,
    val deploymentStatus: StudyDeploymentStatus?,
    val deploymentInformation: MasterDeviceDeployment?,
) : Snapshot<Study>
{
    companion object
    {
        fun fromStudy( study: Study ): StudySnapshot
        {
            val status = study.getStatus()
            val deploymentInformation: MasterDeviceDeployment? =
                when ( status )
                {
                    is StudyStatus.DeviceDeploymentReceived -> status.deploymentInformation
                    is StudyStatus.Stopped -> status.deploymentInformation
                    else -> null
                }

            return StudySnapshot(
                study.studyDeploymentId,
                study.deviceRoleName,
                study.createdOn,
                (status as? StudyStatus.DeploymentStatusAvailable)?.deploymentStatus,
                deploymentInformation
            )
        }
    }

    override fun toObject(): Study = Study.fromSnapshot(this)
}
