package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.studies.domain.users.StudyOwner


/**
 * Represents a study which can be pilot tested and eventually 'go live', for which a recruitment goal can be set, and participants can be recruited.
 */
class Study(
    /**
     * The person or group that created this [Study].
     */
    val owner: StudyOwner,
    /**
     * A descriptive name for the study, assigned by, and only visible to, the [StudyOwner].
     */
    val name: String,
    /**
     * A description of the study, shared with participants once they are invited to the study.
     */
    val invitation: StudyInvitation = StudyInvitation.empty(),
    val id: UUID = UUID.randomUUID()
)
{
    companion object Factory
    {
        fun fromSnapshot( snapshot: StudySnapshot ): Study
        {
            val study = Study( StudyOwner( snapshot.ownerId ), snapshot.name, snapshot.invitation, snapshot.studyId )
            study.creationDate = snapshot.creationDate

            return study
        }
    }


    /**
     * The date when this study was created.
     */
    var creationDate: DateTime = DateTime.now()
        private set

    /**
     * Get the status (serializable) of this [Study].
     */
    fun getStatus(): StudyStatus = StudyStatus( id, name, creationDate )


    /**
     * Get a serializable snapshot of the current state of this [Study].
     */
    fun getSnapshot(): StudySnapshot
    {
        return StudySnapshot.fromStudy( this )
    }
}
