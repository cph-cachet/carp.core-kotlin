package dk.cachet.carp.studies.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudyOwner
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudyStatus


/**
 * Implementation of [StudyService] which allows creating and managing studies.
 */
class StudyServiceHost( private val repository: StudyRepository ) : StudyService
{
    /**
     * Create a new study for the specified [owner].
     *
     * @param name A descriptive name for the study, assigned by, and only visible to, the [owner].
     * @param invitation
     *  An optional description of the study, shared with participants once they are invited.
     *  In case no description is specified, [name] is used as the name in [invitation].
     */
    override suspend fun createStudy( owner: StudyOwner, name: String, invitation: StudyInvitation? ): StudyStatus
    {
        val ensuredInvitation = invitation ?: StudyInvitation( name )
        val study = Study( owner, name, ensuredInvitation )

        repository.add( study )

        return study.getStatus()
    }

    /**
     * Get the status for a study with the given [studyId].
     *
     * @param studyId The id of the study to return [StudyStatus] for.
     *
     * @throws IllegalArgumentException when a deployment with [studyId] does not exist.
     */
    override suspend fun getStudyStatus( studyId: UUID ): StudyStatus
    {
        val study = repository.getById( studyId )
        require( study != null )

        return study.getStatus()
    }

    /**
     * Get status for all studies created by the specified [owner].
     */
    override suspend fun getStudiesOverview( owner: StudyOwner ): List<StudyStatus> =
        repository.getForOwner( owner ).map { it.getStatus() }
}
