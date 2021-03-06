package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.users.Participant
import kotlinx.serialization.Serializable


@Serializable
data class RecruitmentSnapshot(
    val studyId: UUID,
    override val creationDate: DateTime,
    val studyProtocol: StudyProtocolSnapshot?,
    val invitation: StudyInvitation?,
    val participants: Set<Participant>,
    /**
     * Per study deployment ID, the IDs of the participants participating in it.
     */
    val participations: Map<UUID, Set<UUID>>
) : Snapshot<Recruitment>
{
    companion object
    {
        /**
         * Create a snapshot of the specified [recruitment].
         */
        fun fromParticipantRecruitment( recruitment: Recruitment ): RecruitmentSnapshot
        {
            val status = recruitment.getStatus()
            var studyProtocol: StudyProtocolSnapshot? = null
            var invitation: StudyInvitation? = null
            if ( status is RecruitmentStatus.ReadyForDeployment )
            {
                studyProtocol = status.studyProtocol
                invitation = status.invitation
            }
            val participations = recruitment.participations.mapValues {
                (_, participants) -> participants.map { it.id }.toSet()
            }

            return RecruitmentSnapshot(
                recruitment.studyId,
                recruitment.creationDate,
                studyProtocol,
                invitation,
                recruitment.participants,
                participations )
        }
    }

    override fun toObject(): Recruitment = Recruitment.fromSnapshot( this )
}
