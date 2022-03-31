package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLoggingProxy
import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot


/**
 * A proxy for a protocol [service] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests and published events in [loggedRequests].
 */
class ProtocolServiceLoggingProxy(
    service: ProtocolService,
    eventBus: EventBus,
    log: (LoggedRequest<ProtocolService, ProtocolService.Event>) -> Unit = { }
) :
    ApplicationServiceLoggingProxy<ProtocolService, ProtocolService.Event>(
        service,
        EventBusLog(
            eventBus,
            EventBusLog.Subscription( ProtocolService::class, ProtocolService.Event::class )
        ),
        log
    ),
    ProtocolService
{
    override suspend fun add( protocol: StudyProtocolSnapshot, versionTag: String ) =
        log( ProtocolServiceRequest.Add( protocol, versionTag ) )

    override suspend fun addVersion( protocol: StudyProtocolSnapshot, versionTag: String ) =
        log( ProtocolServiceRequest.AddVersion( protocol, versionTag) )

    override suspend fun updateParticipantDataConfiguration(
        protocolId: UUID,
        versionTag: String,
        expectedParticipantData: Set<ExpectedParticipantData>
    ): StudyProtocolSnapshot = log(
        ProtocolServiceRequest.UpdateParticipantDataConfiguration( protocolId, versionTag, expectedParticipantData )
    )

    override suspend fun getBy( protocolId: UUID, versionTag: String? ): StudyProtocolSnapshot =
        log( ProtocolServiceRequest.GetBy( protocolId, versionTag ) )

    override suspend fun getAllForOwner( ownerId: UUID ): List<StudyProtocolSnapshot> =
        log( ProtocolServiceRequest.GetAllForOwner( ownerId ) )

    override suspend fun getVersionHistoryFor( protocolId: UUID ): List<ProtocolVersion> =
        log( ProtocolServiceRequest.GetVersionHistoryFor( protocolId ) )
}
