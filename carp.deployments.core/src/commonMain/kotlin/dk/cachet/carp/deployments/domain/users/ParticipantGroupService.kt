package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.throwIfInvalid


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
        createdDeployment.protocol.throwIfInvalid( invitations, createdDeployment.connectedDevicePreregistrations )

        // Create group.
        val protocol = createdDeployment.protocol.toObject()
        val group = ParticipantGroup.fromNewDeployment( studyDeploymentId, protocol )

        // Send out invitations and add to group.
        for ( invitation in invitations )
        {
            val participation = Participation( studyDeploymentId, invitation.participantId )
            val assignedDevices = invitation.assignedMasterDeviceRoleNames.map { role ->
                protocol.masterDevices.first { it.roleName == role }
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
