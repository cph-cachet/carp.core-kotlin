package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.Username
import dk.cachet.carp.deployment.domain.users.Participation


/**
 * Application service which allows creating [Account]'s and register in which study deployments they participate.
 */
interface UserService
{
    /**
     * Create an account which is identified by a unique [username].
     *
     * @throws IllegalArgumentException when an [Account] with the specified [username] already exists.
     */
    suspend fun createAccount( username: Username ): Account

    /**
     * Create an account which is identified by an [emailAddress] someone has access to.
     * In case no [Account] is associated with the specified [emailAddress], send out a confirmation email.
     */
    suspend fun createAccount( emailAddress: EmailAddress )

    /**
     * Let the person with the specified [identity] participate in the study deployment with [studyDeploymentId].
     * In case no account is associated to the specified identity, a new account is created.
     * Account details should either be sent when deployment starts, or be retrievable for the person managing the specified [identity].
     */
    suspend fun addParticipation( studyDeploymentId: UUID, identity: AccountIdentity ): Participation

    /**
     * Get all participations included in a study deployment for the given [studyDeploymentId].
     */
    suspend fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation>
}
