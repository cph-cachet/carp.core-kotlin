package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.AccountService
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationRepository


/**
 * Implementation of [ParticipationService] which allows registering participations for study deployments.
 */
class ParticipationServiceHost( private val repository: ParticipationRepository, private val accountService: AccountService ) :
    ParticipationService
{
    /**
     * Let the person with the specified [identity] participate in the study deployment with [studyDeploymentId].
     * In case no account is associated to the specified [identity], a new account is created.
     * An invitation (including account details) is delivered to the person managing the [identity],
     * or should be handed out manually by the person managing the specified [identity].
     */
    override suspend fun addParticipation( studyDeploymentId: UUID, identity: AccountIdentity ): Participation
    {
        var account = accountService.findAccount( identity )
        val isNewAccount = account == null

        // Retrieve or create participation.
        var participation =
            if ( isNewAccount ) null
            else repository.getParticipations( account!!.id ).firstOrNull { it.studyDeploymentId == studyDeploymentId }
        val isNewParticipation = participation == null
        participation = participation ?: Participation( studyDeploymentId )

        // Ensure an account exists for the given identity and an invitation has been sent out.
        if ( isNewAccount )
        {
            account = accountService.inviteNewAccount( identity )
        }
        else if ( isNewParticipation )
        {
            accountService.inviteExistingAccount( identity )
        }

        // Add participation to repository.
        if ( isNewParticipation )
        {
            repository.addParticipation( account!!.id, participation )
        }

        return participation
    }

    /**
     * Get all participations included in a study deployment for the given [studyDeploymentId].
     */
    override suspend fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation>
    {
        return repository.getParticipationsForStudyDeployment( studyDeploymentId )
    }
}
