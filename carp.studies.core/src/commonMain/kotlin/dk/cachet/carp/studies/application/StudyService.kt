package dk.cachet.carp.studies.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.StudyDescription
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
     * @param description
     *  An optional description of the study, visible to all participants.
     *  In case no description is specified, [name] is used as the name in [description].
     */
    suspend fun createStudy( owner: StudyOwner, name: String, description: StudyDescription? = null ): StudyStatus

    /**
     * Get the status for a study with the given [studyId].
     *
     * @param studyId The id of the study to return [StudyStatus] for.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun getStudyStatus( studyId: UUID ): StudyStatus
}
