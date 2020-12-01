package dk.cachet.carp.protocols.application

import dk.cachet.carp.protocols.domain.ProtocolOwner
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
        repository.add( initializedProtocol, versionTag )
    }

    /**
     * Store an updated version of the specified study [protocol].
     *
     * @param protocol An updated version of a [StudyProtocolSnapshot] already stored.
     * @param versionTag An optional unique label used to identify this specific version of the [protocol]. The current date/time by default.
     * @throws IllegalArgumentException when:
     *   - [protocol] is not yet stored in the repository
     *   - [protocol] is invalid
     *   - the [versionTag] is already in use
     */
    override suspend fun update( protocol: StudyProtocolSnapshot, versionTag: String )
    {
        val initializedProtocol = StudyProtocol.fromSnapshot( protocol )
        repository.update( initializedProtocol, versionTag )
    }

    /**
     * Find the [StudyProtocolSnapshot] with the specified [protocolName] owned by [owner].
     *
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     * @throws IllegalArgumentException when the [owner], [protocolName], or [versionTag] does not exist.
     */
    override suspend fun getBy( owner: ProtocolOwner, protocolName: String, versionTag: String? ): StudyProtocolSnapshot
    {
        val protocol: StudyProtocol = repository.getBy( owner, protocolName, versionTag )
        return protocol.getSnapshot()
    }

    /**
     * Find all [StudyProtocolSnapshot]'s owned by [owner].
     *
     * @throws IllegalArgumentException when the [owner] does not exist.
     * @return This returns the last version of each [StudyProtocolSnapshot] owned by the specified [owner].
     */
    override suspend fun getAllFor( owner: ProtocolOwner ): List<StudyProtocolSnapshot>
    {
        val protocols: Sequence<StudyProtocol> = repository.getAllFor( owner )
        return protocols.map { it.getSnapshot() }.toList()
    }

    /**
     * Returns all stored versions for the [StudyProtocol] owned by [owner] with [protocolName].
     */
    override suspend fun getVersionHistoryFor( owner: ProtocolOwner, protocolName: String ): List<ProtocolVersion>
    {
        return repository.getVersionHistoryFor( owner, protocolName )
    }
}
