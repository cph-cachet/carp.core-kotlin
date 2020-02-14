package dk.cachet.carp.studies.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.studies.domain.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.test.Mock


class StudyServiceMock(
    private val createStudyResult: StudyStatus = StudyStatus( UUID.randomUUID(), "Test" ),
    private val getStudyStatusResult: StudyStatus = StudyStatus( UUID.randomUUID(), "Test" ),
    private val getStudiesOverview: List<StudyStatus> = listOf()
) : Mock<StudyService>(), StudyService
{
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
