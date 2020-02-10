package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.Participation


/**
 * Application service which allows registering participations for study deployments.
 */
interface ParticipationService
{
    /**
     * Let the person with the specified [identity] participate in the study deployment with [studyDeploymentId].
     * In case no account is associated to the specified identity, a new account is created.
     * Account details are sent to the person holding the identity, or made retrievable for the person managing the specified [identity].
     */
    suspend fun addParticipation( studyDeploymentId: UUID, identity: AccountIdentity ): Participation

    /**
     * Get all participations included in a study deployment for the given [studyDeploymentId].
     */
    suspend fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation>
}
