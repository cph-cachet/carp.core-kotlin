package bhrp.studyprotocol.domain


/**
 * A repository which handles persisting [StudyProtocol]'s and store updated versions for them.
 *
 * Protocol names are unique to a [ProtocolOwner].
 * Version tags are unique to a [StudyProtocol].
 */
interface StudyProtocolRepository
{
    /**
     * Add the specified study [protocol] to the repository.
     *
     * @param protocol The [StudyProtocol] to add.
     * @param versionTag A label used to identify this first initial version of the [protocol].
     * @throws IllegalArgumentException when the [protocol] already exists.
     */
    fun add( protocol: StudyProtocol, versionTag: String )

    /**
     * Store an updated version of the specified study [protocol] in the repository.
     *
     * @param protocol An updated version of a [StudyProtocol] already stored in the repository.
     * @param versionTag A unique label used to identify this specific version of the [protocol].
     * @throws IllegalArgumentException when the [protocol] is not yet stored in the repository or when the [versionTag] is already in use.
     */
    fun update( protocol: StudyProtocol, versionTag: String )

    /**
     * Find the [StudyProtocol] with the specified [protocolName] owned by [owner].
     *
     * @param owner The owner of the protocol to return.
     * @param protocolName The name of the protocol to return.
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     * @throws IllegalArgumentException when the [owner], [protocolName], or [versionTag] does not exist.
     */
    fun getBy( owner: ProtocolOwner, protocolName: String, versionTag: String? = null ): StudyProtocol

    /**
     * Find all [StudyProtocol]'s owned by [owner].
     *
     * @throws IllegalArgumentException when the [owner] does not exist.
     * @return This returns the last version of each [StudyProtocol] owned by the specified [owner].
     */
    fun getAllFor( owner: ProtocolOwner ): Sequence<StudyProtocol>

    /**
     * Returns all stored versions for the [StudyProtocol] owned by [owner] with [protocolName].
     */
    fun getVersionHistoryFor( owner: ProtocolOwner, protocolName: String ): List<ProtocolVersion>
}