package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.ParticipantAttribute
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.ProtocolVersion
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.test.Mock


class ProtocolServiceMock(
    val updateParticipantDataConfigurationResult: StudyProtocolSnapshot = StudyProtocol( ProtocolOwner(), "Mock" ).getSnapshot(),
    val getByResult: StudyProtocolSnapshot = StudyProtocol( ProtocolOwner(), "Mock" ).getSnapshot(),
    val getAllForResult: List<StudyProtocolSnapshot> = listOf(),
    val getVersionHistoryForResult: List<ProtocolVersion> = listOf()
) : Mock<ProtocolService>(), ProtocolService
{
    override suspend fun add( protocol: StudyProtocolSnapshot, versionTag: String ) =
        trackSuspendCall( ProtocolService::add, protocol, versionTag )

    override suspend fun addVersion( protocol: StudyProtocolSnapshot, versionTag: String ) =
        trackSuspendCall( ProtocolService::addVersion, protocol, versionTag )

    override suspend fun updateParticipantDataConfiguration(
        protocolId: StudyProtocol.Id,
        versionTag: String,
        expectedParticipantData: Set<ParticipantAttribute>
    ): StudyProtocolSnapshot
    {
        trackSuspendCall( ProtocolService::updateParticipantDataConfiguration, protocolId, versionTag, expectedParticipantData )
        return updateParticipantDataConfigurationResult
    }

    override suspend fun getBy( protocolId: StudyProtocol.Id, versionTag: String? ): StudyProtocolSnapshot
    {
        trackSuspendCall( ProtocolService::getBy, protocolId, versionTag )
        return getByResult
    }

    override suspend fun getAllFor( ownerId: UUID ): List<StudyProtocolSnapshot>
    {
        trackSuspendCall( ProtocolService::getAllFor, ownerId )
        return getAllForResult
    }

    override suspend fun getVersionHistoryFor( protocolId: StudyProtocol.Id ): List<ProtocolVersion>
    {
        trackSuspendCall( ProtocolService::getVersionHistoryFor, protocolId )
        return getVersionHistoryForResult
    }
}
