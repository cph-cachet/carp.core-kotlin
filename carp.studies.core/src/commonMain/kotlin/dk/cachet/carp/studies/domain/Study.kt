package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID


/**
 * Represents a study which can be pilot tested and eventually 'go live', for which a recruitment goal can be set, and participants can be recruited.
 */
class Study(
    /**
     * The person or group that created this [Study].
     */
    val owner: StudyOwner,
    /**
     * A descriptive name for the study assigned by the [StudyOwner].
     */
    val name: String,
    val id: UUID = UUID.randomUUID() )
{
    companion object Factory
    {
        fun fromSnapshot( snapshot: StudySnapshot ): Study
        {
            return Study( StudyOwner( snapshot.ownerId ), snapshot.name, snapshot.studyId )
        }
    }


    /**
     * Get a serializable snapshot of the current state of this [Study].
     */
    fun getSnapshot(): StudySnapshot
    {
        return StudySnapshot.fromStudy( this )
    }
}