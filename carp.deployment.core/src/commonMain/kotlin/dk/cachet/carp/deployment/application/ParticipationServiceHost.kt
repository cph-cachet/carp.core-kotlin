package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.AccountRepository
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationRepository


/**
 * Implementation of [ParticipationService] which allows registering participations for study deployments.
 */
class ParticipationServiceHost( private val repository: ParticipationRepository, private val accountRepository: AccountRepository ) :
    ParticipationService
{
    /**
     * Let the person with the specified [identity] participate in the study deployment with [studyDeploymentId].
     * In case no account is associated to the specified identity, a new account is created.
     * Account details should either be sent when deployment starts, or be retrievable for the person managing the specified [identity].
     */
    override suspend fun addParticipation( studyDeploymentId: UUID, identity: AccountIdentity ): Participation
    {
        var account = accountRepository.findAccountWithIdentity( identity )
        val isNewAccount = account == null
        var participation =
            if ( isNewAccount ) null
            else repository.getParticipations( account!!.id ).firstOrNull { it.studyDeploymentId == studyDeploymentId }

        // Create an account for the given identity if it does not yet exist.
        if ( isNewAccount )
        {
            account = Account( identity )
            accountRepository.addAccount( account )
        }

        // Create and add participation if it does not yet exist.
        if ( participation == null )
        {
            participation = Participation( studyDeploymentId )
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
