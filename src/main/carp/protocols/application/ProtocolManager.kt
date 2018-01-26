package carp.protocols.application

import carp.protocols.domain.*
import java.time.LocalDateTime


/**
 * Application service which allows managing (multiple versions of) [StudyProtocol]'s.
 */
class ProtocolManager( val repository: StudyProtocolRepository )
{
    /**
     * Add the specified study [protocol].
     *
     * @param protocol The [StudyProtocol] to add.
     * @param versionTag An optional label used to identify this first version of the [protocol]. "Initial" by default.
     * @throws IllegalArgumentException when the [protocol] already exists.
     */
    fun add( protocol: StudyProtocol, versionTag: String = "Initial" )
    {
        repository.add( protocol, versionTag )
    }

    /**
     * Store an updated version of the specified study [protocol].
     *
     * @param protocol An updated version of a [StudyProtocol] already stored.
     * @param versionTag An optional unique label used to identify this specific version of the [protocol]. The current date/time by default.
     * @throws IllegalArgumentException when the [protocol] is not yet stored in the repository or when the [versionTag] is already in use.
     */
    fun update( protocol: StudyProtocol, versionTag: String = LocalDateTime.now().toString() )
    {
        repository.update( protocol, versionTag )
    }

    /**
     * Find the [StudyProtocol] with the specified [protocolName] owned by [owner].
     *
     * @param owner The owner of the protocol to return.
     * @param protocolName The name of the protocol to return.
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     * @throws IllegalArgumentException when the [owner], [protocolName], or [versionTag] does not exist.
     */
    fun getBy( owner: ProtocolOwner, protocolName: String, versionTag: String? = null ): StudyProtocol
    {
        return repository.getBy( owner, protocolName, versionTag )
    }

    /**
     * Find all [StudyProtocol]'s owned by [owner].
     *
     * @throws IllegalArgumentException when the [owner] does not exist.
     * @return This returns the last version of each [StudyProtocol] owned by the specified [owner].
     */
    fun getAllFor( owner: ProtocolOwner ): Sequence<StudyProtocol>
    {
        return repository.getAllFor( owner )
    }

    /**
     * Returns all stored versions for the [StudyProtocol] owned by [owner] with [protocolName].
     */
    fun getVersionHistoryFor( owner: ProtocolOwner, protocolName: String ): List<ProtocolVersion>
    {
        return repository.getVersionHistoryFor( owner, protocolName )
    }
}