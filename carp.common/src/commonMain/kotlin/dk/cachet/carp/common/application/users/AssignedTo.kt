package dk.cachet.carp.common.application.users

import kotlinx.serialization.Serializable


/**
 * Determines who to assign to something that is specified in a study protocol.
 */
@Serializable
sealed class AssignedTo
{
    @Serializable
    object Anyone : AssignedTo()

    @Serializable
    class Roles( val roleNames: Set<String> ) : AssignedTo()
}
