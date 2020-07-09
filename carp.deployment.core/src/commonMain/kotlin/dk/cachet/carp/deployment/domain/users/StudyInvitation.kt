package dk.cachet.carp.deployment.domain.users

import kotlinx.serialization.Serializable


/**
 * A description of a study, shared with participants once they are invited to a study.
 */
@Serializable
data class StudyInvitation(
    /**
     * A descriptive name for the study to be shown to participants.
     */
    val name: String,
    /**
     * A description of the study clarifying to participants what it is about.
     */
    val description: String
)
{
    companion object
    {
        /**
         * Initializes a [StudyInvitation] with blank values for all fields.
         */
        fun empty(): StudyInvitation = StudyInvitation( "", "" )
    }
}
