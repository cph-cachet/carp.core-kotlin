package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Assign a device, identified by the [deviceRole] of a study protocol, to the participant identified by [participantId].
 */
@Serializable
data class AssignParticipantDevice( val participantId: UUID, val deviceRole: String )
