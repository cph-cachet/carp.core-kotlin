package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * An account participating in a study deployment (identified by [accountId]) using [assignedMasterDeviceRoleNames]
 * and the pseudonym [participationId] assigned to this participation.
 */
@Serializable
data class AccountParticipation(
    val accountId: UUID,
    val participationId: UUID,
    val assignedMasterDeviceRoleNames: Set<String>
)
