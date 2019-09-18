package dk.cachet.carp.client.domain

import dk.cachet.carp.common.*
import kotlinx.serialization.Serializable


@Serializable
data class StudyRuntimeSnapshot(
    @Serializable( with = UUIDSerializer::class )
    val studyDeploymentId: UUID,
    val deviceRoleName: String )
{
    companion object
    {
        fun fromStudyRuntime( studyRuntime: StudyRuntime ): StudyRuntimeSnapshot
        {
            return StudyRuntimeSnapshot( studyRuntime.studyDeploymentId, studyRuntime.deviceRoleName )
        }
    }
}