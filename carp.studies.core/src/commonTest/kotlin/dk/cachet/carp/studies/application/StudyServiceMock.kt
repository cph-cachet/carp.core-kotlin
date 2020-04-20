package dk.cachet.carp.studies.application

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.StudyDetails
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.createComplexStudy
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.test.Mock

private typealias Service = StudyService


class StudyServiceMock(
    private val createStudyResult: StudyStatus = studyStatus,
    private val updateInternalDescriptionResult: StudyStatus = studyStatus,
    private val getStudyDetailsResult: StudyDetails = StudyDetails(
        UUID.randomUUID(), StudyOwner(), "Name", DateTime.now(),
        "Description", StudyInvitation.empty(), createComplexStudy().protocolSnapshot
    ),
    private val getStudyStatusResult: StudyStatus = studyStatus,
    private val getStudiesOverviewResult: List<StudyStatus> = emptyList(),
    private val addParticipantResult: Participant = Participant( AccountIdentity.fromEmailAddress( "test@test.com" ) ),
    private val getParticipantsResult: List<Participant> = emptyList(),
    private val setInvitationResult: StudyStatus = studyStatus,
    private val setProtocolResult: StudyStatus = studyStatus,
    private val goLiveResult: StudyStatus = studyStatus,
    private val deployParticipantResult: ParticipantGroupStatus = groupStatus,
    private val getParticipantGroupStatusListResult: List<ParticipantGroupStatus> = emptyList(),
    private val stopParticipantGroupResult: ParticipantGroupStatus = groupStatus
) : Mock<Service>(), Service
{
    companion object
    {
        private val studyStatus = StudyStatus.Configuring(
            UUID.randomUUID(), "Test", DateTime.now(),
            canSetInvitation = true,
            canSetStudyProtocol = false,
            canDeployToParticipants = false,
            canGoLive = true )

        private val groupStatus = ParticipantGroupStatus(
            StudyDeploymentStatus.Invited( UUID.randomUUID(), emptyList() ),
            emptySet() )
    }


    override suspend fun createStudy( owner: StudyOwner, name: String, description: String, invitation: StudyInvitation? ) =
        createStudyResult
        .also { trackSuspendCall( Service::createStudy, owner, name, description, invitation ) }

    override suspend fun setInternalDescription( studyId: UUID, name: String, description: String ) =
        updateInternalDescriptionResult
        .also { trackSuspendCall( Service::setInternalDescription, studyId, name, description ) }

    override suspend fun getStudyDetails( studyId: UUID ) =
        getStudyDetailsResult
        .also { trackSuspendCall( Service::getStudyDetails, studyId ) }

    override suspend fun getStudyStatus( studyId: UUID ) =
        getStudyStatusResult
        .also { trackSuspendCall( Service::getStudyStatus, studyId ) }

    override suspend fun getStudiesOverview( owner: StudyOwner ) =
        getStudiesOverviewResult
        .also { trackSuspendCall( Service::getStudiesOverview, owner ) }

    override suspend fun addParticipant( studyId: UUID, email: EmailAddress ) =
        addParticipantResult
        .also { trackSuspendCall( Service::addParticipant, studyId, email ) }

    override suspend fun getParticipants( studyId: UUID ) =
        getParticipantsResult
        .also { trackSuspendCall( Service::getParticipants, studyId ) }

    override suspend fun setInvitation( studyId: UUID, invitation: StudyInvitation ) =
        setInvitationResult
        .also { trackSuspendCall( Service::setInvitation, studyId, invitation ) }

    override suspend fun setProtocol( studyId: UUID, protocol: StudyProtocolSnapshot ) =
        setProtocolResult
        .also { trackSuspendCall( Service::setProtocol, studyId, protocol ) }

    override suspend fun goLive( studyId: UUID ) =
        goLiveResult
        .also { trackSuspendCall( Service::goLive, studyId ) }

    override suspend fun deployParticipantGroup( studyId: UUID, group: Set<AssignParticipantDevices> ) =
        deployParticipantResult
        .also { trackSuspendCall( Service::deployParticipantGroup, studyId, group ) }

    override suspend fun getParticipantGroupStatusList( studyId: UUID ) =
        getParticipantGroupStatusListResult
        .also { trackSuspendCall( Service::getParticipantGroupStatusList, studyId ) }

    override suspend fun stopParticipantGroup( studyId: UUID, groupId: UUID ) =
        stopParticipantGroupResult
        .also { trackSuspendCall( Service::stopParticipantGroup, studyId, groupId ) }
}
