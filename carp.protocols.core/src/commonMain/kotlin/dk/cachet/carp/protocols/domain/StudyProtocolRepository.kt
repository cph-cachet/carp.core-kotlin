package dk.cachet.carp.protocols.domain


/**
 * A repository which handles persisting different versions of [StudyProtocol]s.
 *
 * Protocol names are unique to a [ProtocolOwner].
 * Version tags are unique to a [StudyProtocol].
 */
interface StudyProtocolRepository
{
    /**
     * Add the specified study [protocol] to the repository.
     *
     * @param version Identifies this first initial version of the [protocol].
     * @throws IllegalArgumentException when a [protocol] with the same owner and name already exists.
     */
    suspend fun add( protocol: StudyProtocol, version: ProtocolVersion )

    /**
     * Add a new [version] for the specified study [protocol] in the repository,
     * of which a previous version with the same owner and name is already stored.
     *
     * @throws IllegalArgumentException when:
     *   - the [protocol] is not yet stored in the repository
     *   - the tag specified in [version] is already in use
     */
    suspend fun addVersion( protocol: StudyProtocol, version: ProtocolVersion )

    /**
     * Return the [StudyProtocol] with the specified [protocolName] owned by [owner],
     * or null when no such protocol is found.
     *
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     */
    suspend fun getBy( owner: ProtocolOwner, protocolName: String, versionTag: String? = null ): StudyProtocol?

    /**
     * Find all [StudyProtocol]'s owned by [owner], or an empty sequence if none are found.
     *
     * @return This returns the last version of each [StudyProtocol] owned by the specified [owner].
     */
    suspend fun getAllFor( owner: ProtocolOwner ): Sequence<StudyProtocol>

    /**
     * Returns all stored versions for the [StudyProtocol] owned by [owner] with [protocolName].
     *
     * @throws IllegalArgumentException when a protocol with [protocolName] for [owner] does not exist.
     */
    suspend fun getVersionHistoryFor( owner: ProtocolOwner, protocolName: String ): List<ProtocolVersion>
}
