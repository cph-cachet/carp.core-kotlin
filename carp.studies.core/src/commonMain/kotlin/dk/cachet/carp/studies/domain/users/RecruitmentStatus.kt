package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.deployment.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.serialization.Serializable


/**
 * Status information about a [Recruitment].
 */
@Serializable
sealed class RecruitmentStatus
{
    /**
     * The [Recruitment] is not yet ready to be deployed to participant groups and awaiting the study to go live.
     */
    object AwaitingStudyToGoLive : RecruitmentStatus()

    /**
     * The [Recruitment] can be deployed to participant groups since the study has gone live.
     */
    data class ReadyForDeployment(
        /**
         * The snapshot of the study protocol which can be deployed to participants in this recruitment.
         */
        val studyProtocol: StudyProtocolSnapshot,
        /**
         * The invitation to send to participants when deployed.
         */
        val invitation: StudyInvitation
    ) : RecruitmentStatus()
}
