package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.ProtocolVersion
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolRepository
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot


/**
 * A [StudyProtocolRepository] which holds study protocols in memory as long as the instance is held in memory.
 */
class InMemoryStudyProtocolRepository : StudyProtocolRepository
{
    private data class StudyProtocolId( val ownerId: UUID, val name: String )


    private val _protocols: MutableMap<StudyProtocolId, MutableMap<ProtocolVersion, StudyProtocolSnapshot>> = mutableMapOf()


    /**
     * Add the specified study [protocol] to the repository.
     *
     * @param versionTag A label used to identify this first initial version of the [protocol].
     * @throws IllegalArgumentException when a [protocol] with the same owner and name already exists.
     */
    override suspend fun add( protocol: StudyProtocol, versionTag: String )
    {
        val id = getId( protocol )
        require( !_protocols.containsKey( id ) )
            { "A protocol with the same owner and name is already stored in this repository." }

        val version = ProtocolVersion( DateTime.now(), versionTag )
        val versions = mutableMapOf( version to protocol.getSnapshot() )
        _protocols[ id ] = versions
    }

    /**
     * Add a new version for the specified study [protocol] in the repository,
     * of which a previous version with the same owner and name is already stored.
     *
     * @param versionTag A unique label used to identify this specific version of the [protocol].
     * @throws IllegalArgumentException when:
     *   - the [protocol] is not yet stored in the repository
     *   - the [versionTag] is already in use
     */
    override suspend fun addVersion( protocol: StudyProtocol, versionTag: String )
    {
        val id = getId( protocol )
        val versions = _protocols[ id ]
        requireNotNull( versions ) { "The specified protocol is not stored in this repository." }
        require( versions.keys.none { it.tag == versionTag } ) { "The version tag is already in use." }

        val newVersion = ProtocolVersion( DateTime.now(), versionTag )
        versions[ newVersion ] = protocol.getSnapshot()
    }

    /**
     * Find the [StudyProtocol] with the specified [protocolName] owned by [owner].
     *
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     * @throws IllegalArgumentException when the [owner], [protocolName], or [versionTag] does not exist.
     */
    override suspend fun getBy( owner: ProtocolOwner, protocolName: String, versionTag: String? ): StudyProtocol
    {
        val id = StudyProtocolId( owner.id, protocolName )
        val versions = _protocols[ id ]
        requireNotNull( versions ) { "A protocol with the specified owner and protocol name does not exist." }

        val selectedVersion =
            if ( versionTag == null ) versions.getLatest()
            else
            {
                versions.keys.firstOrNull { it.tag == versionTag }
            }
        requireNotNull( selectedVersion ) { "No matching version for the requested protocol found." }

        return StudyProtocol.fromSnapshot( versions[ selectedVersion ]!! )
    }

    /**
     * Find all [StudyProtocol]'s owned by [owner].
     *
     * @throws IllegalArgumentException when the [owner] does not exist.
     * @return This returns the last version of each [StudyProtocol] owned by the specified [owner].
     */
    override suspend fun getAllFor( owner: ProtocolOwner ): Sequence<StudyProtocol>
    {
        val ownerProtocols = _protocols
            .filter { it.key.ownerId == owner.id }
            .map {
                val versions = it.value
                val latest = versions.getLatest()
                versions[ latest ]!!
            }
        require( ownerProtocols.isNotEmpty() ) { "There are no protocols for the specified owner." }

        return ownerProtocols.asSequence().map { StudyProtocol.fromSnapshot( it ) }
    }

    /**
     * Returns all stored versions for the [StudyProtocol] owned by [owner] with [protocolName].
     *
     * @throws IllegalArgumentException when a protocol with [protocolName] for [owner] does not exist.
     */
    override suspend fun getVersionHistoryFor( owner: ProtocolOwner, protocolName: String ): List<ProtocolVersion>
    {
        val id = StudyProtocolId( owner.id, protocolName )
        val versions = _protocols[ id ]
        requireNotNull( versions ) { "A protocol with the specified owner and protocol name does not exist." }

        return versions.keys.toList()
    }

    private fun getId( protocol: StudyProtocol ) = StudyProtocolId( protocol.owner.id, protocol.name )
    private fun MutableMap<ProtocolVersion, StudyProtocolSnapshot>.getLatest() =
        this.keys.last() // Versions are always stored in order.
}
