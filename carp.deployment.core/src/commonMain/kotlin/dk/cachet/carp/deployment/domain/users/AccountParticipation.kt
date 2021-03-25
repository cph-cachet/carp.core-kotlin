package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * An account (identified by [accountId]) which was invited to a study deployment (using [invitation])
 * with a pseudonym ID ([participation]) using the devices with [assignedMasterDeviceRoleNames].
 */
@Serializable
data class AccountParticipation(
    val accountId: UUID,
    val participation: Participation,
    val invitation: StudyInvitation,
    val assignedMasterDeviceRoleNames: Set<String>
)
