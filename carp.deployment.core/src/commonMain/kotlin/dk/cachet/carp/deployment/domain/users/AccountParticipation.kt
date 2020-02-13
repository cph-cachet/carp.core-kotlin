package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * An account participating in a study deployment and the pseudonym ID assigned to this participation.
 */
@Serializable
data class AccountParticipation( val accountId: UUID, val participationId: UUID )
