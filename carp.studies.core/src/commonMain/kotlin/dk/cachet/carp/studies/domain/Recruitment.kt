package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.AggregateRoot
import dk.cachet.carp.common.ddd.DomainEvent


/**
 * Represents a set of participants recruited for a [Study] identified by [studyId].
 */
class Recruitment( val studyId: UUID ) :
    AggregateRoot<Recruitment, RecruitmentSnapshot, Recruitment.Event>()
{
    sealed class Event : DomainEvent()

    companion object
    {
        fun fromSnapshot( snapshot: RecruitmentSnapshot ): Recruitment
        {
            val recruitment = Recruitment( snapshot.studyId )
            recruitment.creationDate = snapshot.creationDate

            return recruitment
        }
    }


    /**
     * Get a serializable snapshot of the current state of this [Recruitment].
     */
    override fun getSnapshot(): RecruitmentSnapshot = RecruitmentSnapshot.fromParticipantRecruitment( this )
}
