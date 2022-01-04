@file:Suppress( "ParameterListWrapping" )

package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.createComplexStudy
import dk.cachet.carp.test.Mock
import kotlinx.datetime.Clock

private typealias Service = StudyService


class StudyServiceMock(
    private val createStudyResult: StudyStatus = studyStatus,
    private val updateInternalDescriptionResult: StudyStatus = studyStatus,
    private val getStudyDetailsResult: StudyDetails = StudyDetails(
        UUID.randomUUID(), UUID.randomUUID(), "Name", Clock.System.now(),
        "Description", StudyInvitation( "Some study" ), createComplexStudy().protocolSnapshot
    ),
    private val getStudyStatusResult: StudyStatus = studyStatus,
    private val getStudiesOverviewResult: List<StudyStatus> = emptyList(),
    private val setInvitationResult: StudyStatus = studyStatus,
    private val setProtocolResult: StudyStatus = studyStatus,
    private val goLiveResult: StudyStatus = studyStatus,
    private val removeResult: Boolean = true
) : Mock<Service>(), Service
{
    companion object
    {
        private val studyStatus = StudyStatus.Configuring(
            UUID.randomUUID(), "Test", Clock.System.now(),
            canSetInvitation = true,
            canSetStudyProtocol = false,
            canDeployToParticipants = false,
            canGoLive = true )
    }


    override suspend fun createStudy( ownerId: UUID, name: String, description: String?, invitation: StudyInvitation? ) =
        createStudyResult
        .also { trackSuspendCall( Service::createStudy, ownerId, name, description, invitation ) }

    override suspend fun setInternalDescription( studyId: UUID, name: String, description: String ) =
        updateInternalDescriptionResult
        .also { trackSuspendCall( Service::setInternalDescription, studyId, name, description ) }

    override suspend fun getStudyDetails( studyId: UUID ) =
        getStudyDetailsResult
        .also { trackSuspendCall( Service::getStudyDetails, studyId ) }

    override suspend fun getStudyStatus( studyId: UUID ) =
        getStudyStatusResult
        .also { trackSuspendCall( Service::getStudyStatus, studyId ) }

    override suspend fun getStudiesOverview( ownerId: UUID ) =
        getStudiesOverviewResult
        .also { trackSuspendCall( Service::getStudiesOverview, ownerId ) }

    override suspend fun setInvitation( studyId: UUID, invitation: StudyInvitation ) =
        setInvitationResult
        .also { trackSuspendCall( Service::setInvitation, studyId, invitation ) }

    override suspend fun setProtocol( studyId: UUID, protocol: StudyProtocolSnapshot ) =
        setProtocolResult
        .also { trackSuspendCall( Service::setProtocol, studyId, protocol ) }

    override suspend fun goLive( studyId: UUID ) =
        goLiveResult
        .also { trackSuspendCall( Service::goLive, studyId ) }

    override suspend fun remove( studyId: UUID ): Boolean =
        removeResult
        .also { trackSuspendCall( Service::remove, studyId ) }
}
