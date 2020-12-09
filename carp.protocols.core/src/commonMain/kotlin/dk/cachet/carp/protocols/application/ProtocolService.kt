package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.ProtocolVersion
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot


/**
 * Application service which allows managing (multiple versions of) [StudyProtocolSnapshot]'s,
 * which can be instantiated locally through [StudyProtocol].
 */
interface ProtocolService
{
    /**
     * Add the specified study [protocol].
     *
     * @param versionTag An optional label used to identify this first version of the [protocol]. "Initial" by default.
     * @throws IllegalArgumentException when:
     *   - [protocol] already exists
     *   - [protocol] is invalid
     */
    suspend fun add( protocol: StudyProtocolSnapshot, versionTag: String = "Initial" )

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
    suspend fun addVersion( protocol: StudyProtocolSnapshot, versionTag: String = DateTime.now().toString() )

    /**
     * Find the [StudyProtocolSnapshot] with the specified [protocolName] owned by [owner].
     *
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     * @throws IllegalArgumentException when the [owner], [protocolName], or [versionTag] does not exist.
     */
    suspend fun getBy( owner: ProtocolOwner, protocolName: String, versionTag: String? = null ): StudyProtocolSnapshot

    /**
     * Find all [StudyProtocolSnapshot]'s owned by [owner].
     *
     * @throws IllegalArgumentException when the [owner] does not exist.
     * @return This returns the last version of each [StudyProtocolSnapshot] owned by the specified [owner].
     */
    suspend fun getAllFor( owner: ProtocolOwner ): List<StudyProtocolSnapshot>

    /**
     * Returns all stored versions for the [StudyProtocol] owned by [owner] with [protocolName].
     */
    suspend fun getVersionHistoryFor( owner: ProtocolOwner, protocolName: String ): List<ProtocolVersion>
}
