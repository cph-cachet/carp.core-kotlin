package dk.cachet.carp.studies.application

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.StudyDetails
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.createComplexStudy
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.test.Mock


class StudyServiceMock(
    private val createStudyResult: StudyStatus = studyStatus,
    private val updateInternalDescriptionResult: StudyStatus = studyStatus,
    private val getStudyDetailsResult: StudyDetails = StudyDetails(
        UUID.randomUUID(), StudyOwner(), "Name", DateTime.now(),
        "Description", StudyInvitation.empty(), createComplexStudy().protocolSnapshot
    ),
    private val getStudyStatusResult: StudyStatus = studyStatus,
    private val getStudiesOverviewResult: List<StudyStatus> = listOf(),
    private val addParticipantResult: Participant = Participant( AccountIdentity.fromEmailAddress( "test@test.com" ) ),
    private val getParticipantsResult: List<Participant> = listOf(),
    private val setProtocolResult: StudyStatus = studyStatus,
    private val goLiveResult: StudyStatus = studyStatus,
    private val deployParticipantResult: StudyStatus = studyStatus
) : Mock<StudyService>(), StudyService
{
    companion object
    {
        private val studyStatus = StudyStatus.Configuring(
            UUID.randomUUID(), "Test", DateTime.now(),
            canDeployToParticipants = false,
            canSetStudyProtocol = false,
            canGoLive = true )
    }


    override suspend fun createStudy( owner: StudyOwner, name: String, description: String, invitation: StudyInvitation? ): StudyStatus
    {
        trackSuspendCall( StudyService::createStudy, owner, name, description, invitation )
        return createStudyResult
    }

    override suspend fun updateInternalDescription( studyId: UUID, name: String, description: String ): StudyStatus
    {
        trackSuspendCall( StudyService::updateInternalDescription, studyId, name, description )
        return updateInternalDescriptionResult
    }

    override suspend fun getStudyDetails( studyId: UUID ): StudyDetails
    {
        trackSuspendCall( StudyService::getStudyDetails, studyId )
        return getStudyDetailsResult
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

    override suspend fun goLive( studyId: UUID ): StudyStatus
    {
        trackSuspendCall( StudyService::goLive, studyId )
        return goLiveResult
    }

    override suspend fun deployParticipantGroup( studyId: UUID, group: Set<AssignParticipantDevices> ): StudyStatus
    {
        trackSuspendCall( StudyService::deployParticipantGroup, studyId, group )
        return deployParticipantResult
    }
}
