package dk.cachet.carp.deployment.domain.users

import kotlinx.serialization.Serializable


/**
 * A [participation] in a study deployment and the [invitation] to participate in the study.
 */
@Serializable
data class ParticipationInvitation( val participation: Participation, val invitation: StudyInvitation )
