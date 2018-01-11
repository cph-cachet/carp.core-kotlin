package bhrp.studyprotocol.domain


/**
 * A repository which handles persisting [StudyProtocol]'s.
 *
 * Protocol names are unique to a [ProtocolOwner].
 */
interface StudyProtocolRepository
{
    /**
     * Add the specified study [protocol] to the repository.
     *
     * @throws IllegalArgumentException when the [protocol] already exists.
     */
    fun add( protocol: StudyProtocol )

    /**
     * Save changes for the specified study [protocol] to the repository.
     *
     * @throws IllegalArgumentException when the [protocol] is not yet stored in the repository.
     */
    fun save( protocol: StudyProtocol )

    /**
     * Find the [StudyProtocol] with the specified [protocolName] owned by [owner].
     *
     * @throws IllegalArgumentException when the [owner] or [protocolName] does not exist.
     */
    fun findBy( owner: ProtocolOwner, protocolName: String ): StudyProtocol

    /**
     * Find all [StudyProtocol]'s owned by [owner].
     *
     * @throws IllegalArgumentException when the [owner] does not exist.
     */
    fun findAllFor( owner: ProtocolOwner ): Sequence<StudyProtocol>
}