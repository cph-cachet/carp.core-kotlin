package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker
import dk.cachet.carp.common.infrastructure.services.Command
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot


class ProtocolServiceDecorator(
    service: ProtocolService,
    requestDecorator: (Command<ProtocolServiceRequest<*>>) -> Command<ProtocolServiceRequest<*>>
) : ApplicationServiceDecorator<ProtocolService, ProtocolServiceRequest<*>>(
        service,
        ProtocolServiceInvoker,
        requestDecorator
    ),
    ProtocolService
{
    override suspend fun add( protocol: StudyProtocolSnapshot, versionTag: String ): Unit = invoke(
        ProtocolServiceRequest.Add( protocol, versionTag )
    )

    override suspend fun addVersion( protocol: StudyProtocolSnapshot, versionTag: String ): Unit = invoke(
        ProtocolServiceRequest.AddVersion( protocol, versionTag )
    )

    override suspend fun updateParticipantDataConfiguration(
        protocolId: UUID,
        versionTag: String,
        expectedParticipantData: Set<ExpectedParticipantData>
    ): StudyProtocolSnapshot = invoke(
        ProtocolServiceRequest.UpdateParticipantDataConfiguration( protocolId, versionTag, expectedParticipantData )
    )

    override suspend fun getBy( protocolId: UUID, versionTag: String? ): StudyProtocolSnapshot = invoke(
        ProtocolServiceRequest.GetBy( protocolId, versionTag )
    )

    override suspend fun getAllForOwner( ownerId: UUID ): List<StudyProtocolSnapshot> = invoke(
        ProtocolServiceRequest.GetAllForOwner( ownerId )
    )

    override suspend fun getVersionHistoryFor( protocolId: UUID ): List<ProtocolVersion> = invoke(
        ProtocolServiceRequest.GetVersionHistoryFor( protocolId )
    )
}


object ProtocolServiceInvoker : ApplicationServiceInvoker<ProtocolService, ProtocolServiceRequest<*>>
{
    override suspend fun ProtocolServiceRequest<*>.invoke( service: ProtocolService ): Any =
        when ( this )
        {
            is ProtocolServiceRequest.Add -> service.add( protocol, versionTag )
            is ProtocolServiceRequest.AddVersion -> service.addVersion( protocol, versionTag )
            is ProtocolServiceRequest.UpdateParticipantDataConfiguration ->
                service.updateParticipantDataConfiguration( protocolId, versionTag, expectedParticipantData )
            is ProtocolServiceRequest.GetBy -> service.getBy( protocolId, versionTag )
            is ProtocolServiceRequest.GetAllForOwner -> service.getAllForOwner( ownerId )
            is ProtocolServiceRequest.GetVersionHistoryFor -> service.getVersionHistoryFor( protocolId )
        }
}
