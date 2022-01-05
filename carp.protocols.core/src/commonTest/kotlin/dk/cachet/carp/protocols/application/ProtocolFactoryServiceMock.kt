package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.test.Mock


class ProtocolFactoryServiceMock(
    private val createCustomProtocolResult: StudyProtocolSnapshot =
        StudyProtocol( UUID.randomUUID(), "Mock" ).getSnapshot()
) : Mock<ProtocolFactoryService>(), ProtocolFactoryService
{
    override suspend fun createCustomProtocol(
        ownerId: UUID,
        name: String,
        customProtocol: String,
        description: String?
    ): StudyProtocolSnapshot
    {
        trackSuspendCall( ProtocolFactoryService::createCustomProtocol, ownerId, name, customProtocol, description )
        return createCustomProtocolResult
    }
}
