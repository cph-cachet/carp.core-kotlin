package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.protocols.application.ProtocolVersion


/**
 * A repository which handles persisting different versions of [StudyProtocol]s.
 *
 * Protocol names are unique to a protocol owner.
 * Version tags are unique to a [StudyProtocol].
 */
interface StudyProtocolRepository
{
    /**
     * Add the specified study [protocol] to the repository.
     *
     * @param version Identifies this first initial version of the [protocol].
     * @throws IllegalArgumentException when:
     *   - a [protocol] with the same id already exists
     *   - a different [protocol] with the same owner and name in the latest version already exists
     */
    suspend fun add( protocol: StudyProtocol, version: ProtocolVersion )

    /**
     * Add a new [version] for the specified study [protocol] in the repository,
     * of which a previous version with the same id is already stored.
     *
     * @throws IllegalArgumentException when:
     *   - the [protocol] is not yet stored in the repository
     *   - the tag specified in [version] is already in use
     *   - a different [protocol] with the same owner and name in the latest version already exists
     */
    suspend fun addVersion( protocol: StudyProtocol, version: ProtocolVersion )

    /**
     * Replace a [version] of a [protocol], of which a previous version with the same owner and name is already stored.
     *
     * @throws IllegalArgumentException when:
     *   - the [protocol] with [version] to replace is not found
     *   - a different [protocol] with the same owner and name in the latest version already exists
     */
    suspend fun replace( protocol: StudyProtocol, version: ProtocolVersion )

    /**
     * Return the [StudyProtocol] with the specified protocol [id], or null when no such protocol is found.
     *
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     */
    suspend fun getBy( id: UUID, versionTag: String? = null ): StudyProtocol?

    /**
     * Return the [StudyProtocol] with the specified [id].
     *
     * @throws IllegalArgumentException when the requested protocol is not found.
     */
    suspend fun getByOrThrow( id: UUID, versionTag: String? = null ): StudyProtocol =
        getBy( id, versionTag )
            ?: throw IllegalArgumentException( "A protocol with ID \"$id\" and the specified version tag does not exist." )

    /**
     * Find all [StudyProtocol]'s owned by the owner with [ownerId], or an empty sequence if none are found.
     *
     * @return This returns the last version of each [StudyProtocol] owned by the requested owner.
     */
    suspend fun getAllForOwner( ownerId: UUID ): Sequence<StudyProtocol>

    /**
     * Returns all stored versions for the [StudyProtocol] with the specified [id].
     *
     * @throws IllegalArgumentException when a protocol with the specified [id] does not exist.
     */
    suspend fun getVersionHistoryFor( id: UUID ): List<ProtocolVersion>
}
