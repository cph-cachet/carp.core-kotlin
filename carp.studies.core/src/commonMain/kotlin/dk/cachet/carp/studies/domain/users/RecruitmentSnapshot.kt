package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import kotlinx.serialization.Serializable


@Serializable
data class RecruitmentSnapshot(
    val studyId: UUID,
    override val creationDate: DateTime,
    val studyProtocol: StudyProtocolSnapshot?,
    val invitation: StudyInvitation?,
    val participants: Set<Participant>,
    val participations: Map<UUID, Set<DeanonymizedParticipation>>
) : Snapshot<Recruitment>
{
    companion object
    {
        /**
         * Create a snapshot of the specified [recruitment].
         */
        fun fromParticipantRecruitment( recruitment: Recruitment ): RecruitmentSnapshot
        {
            val clonedParticipations: MutableMap<UUID, Set<DeanonymizedParticipation>> = mutableMapOf()
            for ( p in recruitment.participations )
            {
                clonedParticipations[ p.key ] = p.value.toSet()
            }

            val status = recruitment.getStatus()
            var studyProtocol: StudyProtocolSnapshot? = null
            var invitation: StudyInvitation? = null
            if ( status is RecruitmentStatus.ReadyForDeployment )
            {
                studyProtocol = status.studyProtocol
                invitation = status.invitation
            }

            return RecruitmentSnapshot(
                recruitment.studyId,
                recruitment.creationDate,
                studyProtocol,
                invitation,
                recruitment.participants,
                participations = clonedParticipations )
        }
    }

    override fun toObject(): Recruitment = Recruitment.fromSnapshot( this )
}
