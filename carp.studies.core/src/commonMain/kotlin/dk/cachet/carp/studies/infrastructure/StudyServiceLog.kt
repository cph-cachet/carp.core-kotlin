package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyStatus


/**
 * A proxy for a study [service] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests in [loggedRequests].
 */
class StudyServiceLog(
    service: StudyService,
    eventBus: EventBus,
    log: (LoggedRequest<StudyService, StudyService.Event>) -> Unit = { }
) :
    ApplicationServiceLog<StudyService, StudyService.Event>(
        service,
        StudyService::class,
        StudyService.Event::class,
        eventBus,
        log
    ),
    StudyService
{
    override suspend fun createStudy(
        ownerId: UUID,
        name: String,
        description: String?,
        invitation: StudyInvitation?
    ): StudyStatus =
        log( StudyServiceRequest.CreateStudy( ownerId, name, description, invitation ) )

    override suspend fun setInternalDescription( studyId: UUID, name: String, description: String ): StudyStatus =
        log( StudyServiceRequest.SetInternalDescription( studyId, name, description ) )

    override suspend fun getStudyDetails( studyId: UUID ): StudyDetails =
        log( StudyServiceRequest.GetStudyDetails( studyId ) )

    override suspend fun getStudyStatus( studyId: UUID ): StudyStatus =
        log( StudyServiceRequest.GetStudyStatus( studyId ) )

    override suspend fun getStudiesOverview( ownerId: UUID ): List<StudyStatus> =
        log( StudyServiceRequest.GetStudiesOverview( ownerId ) )

    override suspend fun setInvitation( studyId: UUID, invitation: StudyInvitation ): StudyStatus =
        log( StudyServiceRequest.SetInvitation( studyId, invitation ) )

    override suspend fun setProtocol( studyId: UUID, protocol: StudyProtocolSnapshot ): StudyStatus =
        log( StudyServiceRequest.SetProtocol( studyId, protocol ) )

    override suspend fun goLive( studyId: UUID ): StudyStatus =
        log( StudyServiceRequest.GoLive( studyId ) )

    override suspend fun remove( studyId: UUID ): Boolean =
        log( StudyServiceRequest.Remove( studyId ) )
}
