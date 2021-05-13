package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.serialization.Serializable


/**
 * The information which needs to be provided when inviting a participant to a deployment.
 */
@Serializable
data class ParticipantInvitation(
    /**
     * An ID for the participant, uniquely assigned by the calling service.
     */
    val participantId: UUID,
    /**
     * The role names of the master devices in the study protocol which the participant is asked to use.
     */
    val assignedMasterDeviceRoleNames: Set<String>,
    /**
     * The identity used to authenticate and invite the participant.
     */
    val identity: AccountIdentity,
    /**
     * A description of the study which is shared with the participant.
     */
    val invitation: StudyInvitation
)


/**
 * Throw [IllegalArgumentException] when [invitations] do not match the requirements of the protocol.
 *
 * @throws IllegalArgumentException when:
 *  - [invitations] is empty
 *  - any of the assigned device roles in [invitations] is not part of the study protocol
 *  - not all master devices part of the study protocol have been assigned a participant
 */
fun StudyProtocolSnapshot.throwIfInvalid( invitations: List<ParticipantInvitation> )
{
    require( invitations.isNotEmpty() ) { "No participants invited." }

    val masterDevices = this.masterDevices.map { it.roleName }
    val assignedMasterDevices = invitations.flatMap { it.assignedMasterDeviceRoleNames }.toSet()

    assignedMasterDevices.forEach {
        require( it in masterDevices )
            { "The assigned master device with role name \"$it\" is not part of the study protocol." }
    }

    require( assignedMasterDevices.containsAll( masterDevices ) )
        { "Not all devices required for this study have been assigned to a participant." }
}
