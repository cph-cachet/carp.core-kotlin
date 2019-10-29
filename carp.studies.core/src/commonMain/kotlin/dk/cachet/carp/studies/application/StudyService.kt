package dk.cachet.carp.studies.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.*


/**
 * Application service which allows creating and managing [Study]'s.
 */
interface StudyService
{
    /**
     * Create a new study for the specified [owner].
     *
     * @param name A descriptive name for the study, assigned by, and only visible to, the [owner].
     */
    suspend fun createStudy( owner: StudyOwner, name: String ): StudyStatus
}