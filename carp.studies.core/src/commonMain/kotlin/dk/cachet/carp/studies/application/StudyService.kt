package dk.cachet.carp.studies.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.studies.domain.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus


/**
 * Application service which allows creating and managing studies.
 */
interface StudyService
{
    /**
     * Create a new study for the specified [owner].
     *
     * @param name A descriptive name for the study, assigned by, and only visible to, the [owner].
     * @param invitation
     *  An optional description of the study, shared with participants once they are invited.
     *  In case no description is specified, [name] is used as the name in [invitation].
     */
    suspend fun createStudy( owner: StudyOwner, name: String, invitation: StudyInvitation? = null ): StudyStatus

    /**
     * Get the status for a study with the given [studyId].
     *
     * @param studyId The id of the study to return [StudyStatus] for.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun getStudyStatus( studyId: UUID ): StudyStatus

    /**
     * Get status for all studies created by the specified [owner].
     */
    suspend fun getStudiesOverview( owner: StudyOwner ): List<StudyStatus>
}
