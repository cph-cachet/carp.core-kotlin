package dk.cachet.carp.deployment.domain.users

import kotlinx.serialization.Serializable


/**
 * A [participation] in a study deployment and the [invitation] to participate in the study,
 * using the master devices with the specified [deviceRoleNames].
 */
@Serializable
data class ParticipationInvitation(
    val participation: Participation,
    val invitation: StudyInvitation,
    val deviceRoleNames: Set<String>
)
