package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.throwIfInvalidInvitations
import dk.cachet.carp.deployments.application.throwIfInvalidPreregistrations
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot


class ParticipantGroupService( val accountService: AccountService )
{
    /**
     * Create a [ParticipantGroup] for the invited participants defined in the newly [createdDeployment].
     *
     * @throws IllegalArgumentException when the invitations do not match the requirements from the protocol.
     */
    suspend fun createAndInviteParticipantGroup( createdDeployment: DeploymentService.Event.StudyDeploymentCreated ): ParticipantGroup
    {
        // Verify whether the participant group matches the requirements of the protocol.
        val studyDeploymentId = createdDeployment.studyDeploymentId
        val invitations = createdDeployment.invitations
        createdDeployment.protocol.throwIfInvalidInvitations( invitations )
        createdDeployment.protocol.throwIfInvalidPreregistrations( createdDeployment.connectedDevicePreregistrations )

        // Create group.
        val protocolSnapshot = createdDeployment.protocol
        val protocol = protocolSnapshot.toObject()
        val group = ParticipantGroup.fromNewDeployment( studyDeploymentId, protocol )

        // Send out invitations and add to group.
        for ( invitation in invitations )
        {
            val participation = Participation( studyDeploymentId, invitation.assignedRoles, invitation.participantId )
            val assignedDevices = protocolSnapshot.getAssignedDeviceRoleNames( invitation.assignedRoles ).map { role ->
                protocol.primaryDevices.first { it.roleName == role }
            }

            // Ensure an account exists for the given identity and an invitation has been sent out.
            val identity = invitation.identity
            var account = accountService.findAccount( identity )
            val studyInvitation = invitation.invitation
            if ( account == null )
            {
                account = accountService.inviteNewAccount( identity, studyInvitation, participation, assignedDevices )
            }
            else
            {
                accountService.inviteExistingAccount( account.id, studyInvitation, participation, assignedDevices )
            }

            group.addParticipation( account, studyInvitation, participation, assignedDevices.toSet() )
        }

        return group
    }
}


/**
 * Retrieve the role names of all assigned devices for someone with [assignedParticipantRoles] in this protocol.
 *
 * TODO: Should this be made part of `StudyProtocol` instead?
 *
 * @throws IllegalArgumentException if [assignedParticipantRoles] contains a role not part of this protocol.
 */
fun StudyProtocolSnapshot.getAssignedDeviceRoleNames( assignedParticipantRoles: AssignedTo ): Set<String>
{
    if ( assignedParticipantRoles is AssignedTo.Roles )
    {
        require( this.participantRoles.map { it.role }.containsAll( assignedParticipantRoles.roleNames ) )
            { "A participant role which isn't part of this protocol is specified." }
    }

    val primaryDeviceRoleNames = this.primaryDevices.map { it.roleName }

    return when ( assignedParticipantRoles )
    {
        is AssignedTo.All -> primaryDeviceRoleNames // All roles are assigned, thus can use all devices.
        is AssignedTo.Roles -> primaryDeviceRoleNames
            .filter { deviceRole ->
                // No specific participant role assigned to a device means all participants can use it.
                val rolesForDevice =
                    this.assignedDevices[ deviceRole ] ?: return@filter true
                assignedParticipantRoles.roleNames.any { it in rolesForDevice }
            }
    }.toSet()
}
