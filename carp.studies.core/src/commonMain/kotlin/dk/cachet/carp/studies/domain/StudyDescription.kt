package dk.cachet.carp.studies.domain

import kotlinx.serialization.Serializable


/**
 * A description of a [Study], intended to be shared with participants.
 */
@Serializable
data class StudyDescription(
    /**
     * A descriptive name for the [Study] to be shown to participants.
     */
    val name: String
)
{
    companion object
    {
        /**
         * Initializes a [StudyDescription] with blank values for all fields.
         */
        fun empty(): StudyDescription = StudyDescription( "" )
    }
}
