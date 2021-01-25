package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.Snapshot
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.Participant
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

            return RecruitmentSnapshot(
                recruitment.studyId,
                recruitment.creationDate,
                recruitment.studyProtocol,
                recruitment.invitation,
                recruitment.participants,
                participations = clonedParticipations )
        }
    }

    override fun toObject(): Recruitment = Recruitment.fromSnapshot( this )
}
