package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID


interface UserRepository
{
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
