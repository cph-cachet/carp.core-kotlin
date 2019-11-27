package dk.cachet.carp.protocols.application

import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.ProtocolVersion
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.test.Mock


class ProtocolServiceMock(
    val getByResult: StudyProtocolSnapshot = StudyProtocol( ProtocolOwner(), "Mock" ).getSnapshot(),
    val getAllForResult: List<StudyProtocolSnapshot> = listOf(),
    val getVersionHistoryForResult: List<ProtocolVersion> = listOf()
) : Mock<ProtocolService>(), ProtocolService
{
    override suspend fun add( protocol: StudyProtocolSnapshot, versionTag: String ) =
        trackSuspendCall( ProtocolService::add, protocol, versionTag )

    override suspend fun update( protocol: StudyProtocolSnapshot, versionTag: String ) =
        trackSuspendCall( ProtocolService::update, protocol, versionTag )

    override suspend fun getBy( owner: ProtocolOwner, protocolName: String, versionTag: String? ): StudyProtocolSnapshot
    {
        trackSuspendCall( ProtocolService::getBy, owner, protocolName, versionTag )
        return getByResult
    }

    override suspend fun getAllFor( owner: ProtocolOwner ): List<StudyProtocolSnapshot>
    {
        trackSuspendCall( ProtocolService::getAllFor, owner )
        return getAllForResult
    }

    override suspend fun getVersionHistoryFor( owner: ProtocolOwner, protocolName: String ): List<ProtocolVersion>
    {
        trackSuspendCall( ProtocolService::getVersionHistoryFor, owner, protocolName )
        return getVersionHistoryForResult
    }
}
