package dk.cachet.carp.studies.application

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.test.Mock


class StudyServiceMock(
    private val createStudyResult: StudyStatus = studyStatus,
    private val getStudyStatusResult: StudyStatus = studyStatus,
    private val getStudiesOverviewResult: List<StudyStatus> = listOf(),
    private val addParticipantResult: Participant = Participant( AccountIdentity.fromEmailAddress( "test@test.com" ) ),
    private val getParticipantsResult: List<Participant> = listOf(),
    private val setProtocolResult: StudyStatus = studyStatus
) : Mock<StudyService>(), StudyService
{
    companion object
    {
        private val studyStatus = StudyStatus(
            UUID.randomUUID(), "Test", DateTime.now(),
            canDeployToParticipants = false,
            isLive = false )
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
        return getStudiesOverviewResult
    }

    override suspend fun addParticipant( studyId: UUID, email: EmailAddress ): Participant
    {
        trackSuspendCall( StudyService::addParticipant, studyId, email )
        return addParticipantResult
    }

    override suspend fun getParticipants( studyId: UUID ): List<Participant>
    {
        trackSuspendCall( StudyService::getParticipants, studyId )
        return getParticipantsResult
    }

    override suspend fun setProtocol( studyId: UUID, protocol: StudyProtocolSnapshot ): StudyStatus
    {
        trackSuspendCall( StudyService::setProtocol, studyId, protocol )
        return setProtocolResult
    }
}
