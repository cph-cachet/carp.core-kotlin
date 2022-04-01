package dk.cachet.carp.common.application.users

import kotlinx.serialization.Serializable


/**
 * Determines which participant in a protocol to assign something to.
 */
@Serializable
sealed class AssignedTo
{
    /**
     * The assigned object is relevant to anyone in the study protocol.
     */
    @Serializable
    object Anyone : AssignedTo()

    /**
     * The assigned object is relevant to the specified [roleNames] in the study protocol.
     */
    @Serializable
    data class Roles( val roleNames: Set<String> ) : AssignedTo()
}
