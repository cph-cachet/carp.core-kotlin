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
     * @param versionTag A label used to identify this first initial version of the [protocol].
     * @throws IllegalArgumentException when the [protocol] already exists.
     */
    suspend fun add( protocol: StudyProtocol, versionTag: String )

    /**
     * Add a new version for the specified study [protocol] in the repository,
     * of which a previous version with the same owner and name is already stored.
     *
     * @param versionTag A unique label used to identify this specific version of the [protocol].
     * @throws IllegalArgumentException when:
     *   - the [protocol] is not yet stored in the repository
     *   - the [versionTag] is already in use
     */
    suspend fun addVersion( protocol: StudyProtocol, versionTag: String )

    /**
     * Find the [StudyProtocol] with the specified [protocolName] owned by [owner].
     *
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     * @throws IllegalArgumentException when the [owner], [protocolName], or [versionTag] does not exist.
     */
    suspend fun getBy( owner: ProtocolOwner, protocolName: String, versionTag: String? = null ): StudyProtocol

    /**
     * Find all [StudyProtocol]'s owned by [owner].
     *
     * @throws IllegalArgumentException when the [owner] does not exist.
     * @return This returns the last version of each [StudyProtocol] owned by the specified [owner].
     */
    suspend fun getAllFor( owner: ProtocolOwner ): Sequence<StudyProtocol>

    /**
     * Returns all stored versions for the [StudyProtocol] owned by [owner] with [protocolName].
     */
    suspend fun getVersionHistoryFor( owner: ProtocolOwner, protocolName: String ): List<ProtocolVersion>
}
