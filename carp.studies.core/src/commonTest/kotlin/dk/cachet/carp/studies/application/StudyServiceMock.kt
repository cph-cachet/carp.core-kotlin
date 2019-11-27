package dk.cachet.carp.studies.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.*
import dk.cachet.carp.test.Mock


class StudyServiceMock(
    private val createStudyResult: StudyStatus = StudyStatus( UUID.randomUUID(), "Test" ),
    private val getStudyStatusResult: StudyStatus = StudyStatus( UUID.randomUUID(), "Test" )
) : Mock<StudyService>(), StudyService
{
    override suspend fun createStudy( owner: StudyOwner, name: String, description: StudyDescription? ): StudyStatus
    {
        trackSuspendCall( StudyService::createStudy, owner, name, description )
        return createStudyResult
    }

    override suspend fun getStudyStatus( studyId: UUID ): StudyStatus
    {
        trackSuspendCall( StudyService::getStudyStatus, studyId )
        return getStudyStatusResult
    }
}
