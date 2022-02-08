package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot


/**
 * A proxy for a protocol [service] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests in [loggedRequests].
 */
class ProtocolServiceLog( service: ProtocolService, log: (LoggedRequest<ProtocolService>) -> Unit = { } ) :
    ApplicationServiceLog<ProtocolService>( service, log ),
    ProtocolService
{
    override suspend fun add( protocol: StudyProtocolSnapshot, versionTag: String ) =
        log( ProtocolServiceRequest.Add( protocol, versionTag ) )

    override suspend fun addVersion( protocol: StudyProtocolSnapshot, versionTag: String ) =
        log( ProtocolServiceRequest.AddVersion( protocol, versionTag) )

    override suspend fun updateParticipantDataConfiguration(
        protocolId: UUID,
        versionTag: String,
        expectedParticipantData: Set<ParticipantAttribute>
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
