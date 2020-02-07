package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.EmailAccountIdentity
import dk.cachet.carp.common.users.Username
import dk.cachet.carp.common.users.UsernameAccountIdentity
import dk.cachet.carp.deployment.domain.NotifyUserService
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.UserRepository


/**
 * Implementation of [UserService] which allows creating [Account]'s and register in which study deployments they participate.
 */
class UserServiceHost( private val repository: UserRepository, private val notifyUserService: NotifyUserService ) : UserService
{
    /**
     * Create an account which is identified by a unique [username].
     *
     * @throws IllegalArgumentException when an [Account] with the specified [username] already exists.
     */
    override suspend fun createAccount( username: Username ): Account
    {
        require( repository.findAccountWithIdentity( UsernameAccountIdentity( username ) ) == null )

        val account = Account.withUsernameIdentity( username )
        repository.addAccount( account )

        return account
    }

    /**
     * Create an account which is identified by an [emailAddress] someone has access to.
     * In case no [Account] is associated with the specified [emailAddress], send out a verification email.
     */
    override suspend fun createAccount( emailAddress: EmailAddress )
    {
        val existingAccount = repository.findAccountWithIdentity( EmailAccountIdentity( emailAddress ) )
        val isNewAccount = existingAccount == null

        if ( isNewAccount )
        {
            val newAccount = Account.withEmailIdentity( emailAddress )
            repository.addAccount( newAccount )
            notifyUserService.sendAccountVerificationEmail( newAccount.id, emailAddress )
        }
    }

    /**
     * Let the person with the specified [identity] participate in the study deployment with [studyDeploymentId].
     * In case no account is associated to the specified identity, a new account is created.
     * Account details should either be sent when deployment starts, or be retrievable for the person managing the specified [identity].
     */
    override suspend fun addParticipation( studyDeploymentId: UUID, identity: AccountIdentity ): Participation
    {
        var account = repository.findAccountWithIdentity( identity )
        val isNewAccount = account == null
        var participation =
            if ( isNewAccount ) null
            else repository.getParticipations( account!!.id ).firstOrNull { it.studyDeploymentId == studyDeploymentId }

        // Create an account for the given identity if it does not yet exist.
        if ( isNewAccount )
        {
            account = Account( identity )
            repository.addAccount( account )
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
