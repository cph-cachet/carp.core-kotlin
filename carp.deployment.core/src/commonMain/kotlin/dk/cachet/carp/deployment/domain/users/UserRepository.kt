package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity


interface UserRepository
{
    /**
     * Add a new [account] to the repository.
     *
     * @throws IllegalArgumentException when an [account] with the same id or a matching [AccountIdentity] already exists.
     */
    fun addAccount( account: Account )

    /**
     * Returns the [Account] which has the specified [accountId], or null when no account is found.
     */
    fun findAccountWithId( accountId: UUID ): Account?

    /**
     * Returns the [Account] which has the specified [identity], or null when no account is found.
     */
    fun findAccountWithIdentity( identity: AccountIdentity ): Account?

    /**
     * Add [participation] information for a study deployment that an account with the given [accountId] should participate in.
     *
     * @param accountId The ID of the account which acts as a [Participation] in a study.
     * @param participation The [Participation] information of the study to participate in.
     */
    fun addParticipation( accountId: UUID, participation: Participation )

    /**
     * Get [Participation] information for all study deployments an account with the given [accountId] participates in.
     */
    fun getParticipations( accountId: UUID ): List<Participation>

    /**
     * Get all participations included in a study deployment for the given [studyDeploymentId].
     */
    fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation>
}
