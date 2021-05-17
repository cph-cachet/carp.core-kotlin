package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolId
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolRepository


/**
 * A [StudyProtocolRepository] which holds study protocols in memory as long as the instance is held in memory.
 */
class InMemoryStudyProtocolRepository : StudyProtocolRepository
{
    private val _protocols: MutableMap<StudyProtocolId, MutableMap<ProtocolVersion, StudyProtocolSnapshot>> = mutableMapOf()


    /**
     * Add the specified study [protocol] to the repository.
     *
     * @param version Identifies this first initial version of the [protocol].
     * @throws IllegalArgumentException when a [protocol] with the same owner and name already exists.
     */
    override suspend fun add( protocol: StudyProtocol, version: ProtocolVersion )
    {
        require( protocol.id !in _protocols )
            { "A protocol with the same owner and name is already stored in this repository." }

        val versions = mutableMapOf( version to protocol.getSnapshot() )
        _protocols[ protocol.id ] = versions
    }

    /**
     * Add a new [version] for the specified study [protocol] in the repository,
     * of which a previous version with the same owner and name is already stored.
     *
     * @throws IllegalArgumentException when:
     *   - the [protocol] is not yet stored in the repository
     *   - the tag specified in [version] is already in use
     */
    override suspend fun addVersion( protocol: StudyProtocol, version: ProtocolVersion )
    {
        val versions = getVersionsOrThrow( protocol.id )
        require( versions.keys.none { it.tag == version.tag } ) { "The version tag is already in use." }

        versions[ version ] = protocol.getSnapshot()
    }

    /**
     * Replace a [version] of a [protocol], of which a previous version with the same owner and name is already stored.
     *
     * @throws IllegalArgumentException when the [protocol] with [version] to replace is not found.
     */
    override suspend fun replace( protocol: StudyProtocol, version: ProtocolVersion )
    {
        val versions = getVersionsOrThrow( protocol.id )
        require( version in versions.keys ) { "The specified version does not exist." }

        versions[ version ] = protocol.getSnapshot()
    }

    /**
     * Return the [StudyProtocol] with the specified protocol [id], or null when no such protocol is found.
     *
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     */
    override suspend fun getBy( id: StudyProtocolId, versionTag: String? ): StudyProtocol?
    {
        val versions = _protocols[ id ] ?: return null

        val selectedVersion =
            if ( versionTag == null ) versions.getLatest()
            else versions.keys.firstOrNull { it.tag == versionTag }
                ?: return null

        return StudyProtocol.fromSnapshot( versions[ selectedVersion ]!! )
    }

    /**
     * Find all [StudyProtocol]'s owned by the owner with [ownerId], or an empty sequence if none are found.
     *
     * @return This returns the last version of each [StudyProtocol] owned by the requested owner.
     */
    override suspend fun getAllFor( ownerId: UUID ): Sequence<StudyProtocol>
    {
        return _protocols
            .filter { it.key.ownerId == ownerId }
            .map {
                val versions = it.value
                val latest = versions.getLatest()
                versions[ latest ]!!
            }
            .asSequence().map { StudyProtocol.fromSnapshot( it ) }
    }

    /**
     * Returns all stored versions for the [StudyProtocol] with the specified [id].
     *
     * @throws IllegalArgumentException when a protocol with the specified [id] does not exist.
     */
    override suspend fun getVersionHistoryFor( id: StudyProtocolId ): List<ProtocolVersion>
    {
        val versions = getVersionsOrThrow( id )

        return versions.keys.toList()
    }


    private fun getVersionsOrThrow( id: StudyProtocolId ) = _protocols[ id ]
        ?: throw IllegalArgumentException( "The specified protocol is not stored in this repository" )

    private fun MutableMap<ProtocolVersion, StudyProtocolSnapshot>.getLatest() =
        this.keys.last() // Versions are stored in order added. Adding versions quickly (e.g., in tests) can result in same dates.
}
