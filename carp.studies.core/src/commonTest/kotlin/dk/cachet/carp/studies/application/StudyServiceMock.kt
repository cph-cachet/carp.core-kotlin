package dk.cachet.carp.studies.application

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.studies.domain.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.test.Mock


class StudyServiceMock(
    private val createStudyResult: StudyStatus = studyStatus,
    private val getStudyStatusResult: StudyStatus = studyStatus,
    private val getStudiesOverview: List<StudyStatus> = listOf()
) : Mock<StudyService>(), StudyService
{
    companion object
    {
        private val studyStatus = StudyStatus( UUID.randomUUID(), "Test", DateTime.now() )
    }


    override suspend fun createStudy( owner: StudyOwner, name: String, invitation: StudyInvitation? ): StudyStatus
    {
        trackSuspendCall( StudyService::createStudy, owner, name, invitation )
        return createStudyResult
    }

    override suspend fun getStudyStatus( studyId: UUID ): StudyStatus
    {
        trackSuspendCall( StudyService::getStudyStatus, studyId )
        return getStudyStatusResult
    }

    override suspend fun getStudiesOverview( owner: StudyOwner ): List<StudyStatus>
    {
        trackSuspendCall( StudyService::getStudiesOverview, owner )
        return getStudiesOverview
    }
}
