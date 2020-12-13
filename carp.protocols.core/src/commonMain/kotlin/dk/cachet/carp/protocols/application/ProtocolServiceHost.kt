package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.ParticipantAttribute
import dk.cachet.carp.protocols.domain.ProtocolVersion
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolRepository
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot


/**
 * Implementation of [ProtocolService] which allows managing (multiple versions of) [StudyProtocolSnapshot]'s,
 * which can be instantiated locally through [StudyProtocol].
 */
class ProtocolServiceHost( private val repository: StudyProtocolRepository ) : ProtocolService
{
    /**
     * Add the specified study [protocol].
     *
     * @param versionTag An optional label used to identify this first version of the [protocol]. "Initial" by default.
     * @throws IllegalArgumentException when:
     *   - [protocol] already exists
     *   - [protocol] is invalid
     */
    override suspend fun add( protocol: StudyProtocolSnapshot, versionTag: String )
    {
        val initializedProtocol = StudyProtocol.fromSnapshot( protocol )
        repository.add( initializedProtocol, ProtocolVersion( versionTag ) )
    }

    /**
     * Add a new version for the specified study [protocol],
     * of which a previous version with the same owner and name is already stored.
     *
     * @param versionTag An optional unique label used to identify this specific version of the [protocol]. The current date/time by default.
     * @throws IllegalArgumentException when:
     *   - [protocol] is not yet stored in the repository
     *   - [protocol] is invalid
     *   - the [versionTag] is already in use
     */
    override suspend fun addVersion( protocol: StudyProtocolSnapshot, versionTag: String )
    {
        val initializedProtocol = StudyProtocol.fromSnapshot( protocol )
        repository.addVersion( initializedProtocol, ProtocolVersion( versionTag ) )
    }

    /**
     * Replace the expected participant data for the study protocol with the specified [protocolId]
     * and [versionTag] with [expectedParticipantData].
     *
     * @throws IllegalArgumentException when:
     *   - no protocol with [protocolId] is found
     *   - [expectedParticipantData] contains two or more attributes with the same input type.
     * @return The updated [StudyProtocolSnapshot].
     */
    override suspend fun updateParticipantDataConfiguration(
        protocolId: StudyProtocol.Id,
        versionTag: String,
        expectedParticipantData: Set<ParticipantAttribute>
    ): StudyProtocolSnapshot
    {
        val protocol = repository.getByOrThrow( protocolId, versionTag )
        val isReplaced = protocol.replaceExpectedParticipantData( expectedParticipantData )

        if ( isReplaced )
        {
            val version = repository
                .getVersionHistoryFor( protocol.id )
                .first { it.tag == versionTag }
            repository.replace( protocol, version )
        }

        return protocol.getSnapshot()
    }

    /**
     * Return the [StudyProtocolSnapshot] with the specified [protocolId],
     *
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     * @throws IllegalArgumentException when a protocol with [protocolId] or [versionTag] does not exist.
     */
    override suspend fun getBy( protocolId: StudyProtocol.Id, versionTag: String? ): StudyProtocolSnapshot
    {
        val protocol: StudyProtocol? = repository.getBy( protocolId, versionTag )
        requireNotNull( protocol ) { "No protocol found for the specified owner with the given name and version." }

        return protocol.getSnapshot()
    }

    /**
     * Find all [StudyProtocolSnapshot]'s owned by the owner with [ownerId].
     *
     * @return This returns the last version of each [StudyProtocolSnapshot] owned by the requested owner,
     *   or an empty list when none are found.
     */
    override suspend fun getAllFor( ownerId: UUID ): List<StudyProtocolSnapshot> =
        repository.getAllFor( ownerId ).map { it.getSnapshot() }.toList()

    /**
     * Returns all stored versions for the protocol with the specified [protocolId].
     *
     * @throws IllegalArgumentException when a protocol with [protocolId] does not exist.
     */
    override suspend fun getVersionHistoryFor( protocolId: StudyProtocol.Id ): List<ProtocolVersion> =
        repository.getVersionHistoryFor( protocolId )
}
