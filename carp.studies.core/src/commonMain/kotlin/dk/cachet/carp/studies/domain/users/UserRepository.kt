package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.*


interface UserRepository
{
    /**
     * Add a new [account] to the repository.
     *
     * @throws IllegalArgumentException when an [account] with the same ID or email address already exists.
     */
    fun addAccount( account: Account )

    /**
     * Returns the [Account] which has the specified [emailAddress] identity, or null when no account is found.
     */
    fun findAccountWithEmail( emailAddress: EmailAddress ): Account?

    /**
     * Add [participant] information of a study that an account with the given [accountId] should participate in.
     *
     * @param accountId The ID of the account which acts as a [Participant] in a study.
     * @param participant The [Participant] information of a study.
     */
    fun addStudyParticipation( accountId: UUID, participant: Participant )

    /**
     * Get all participants included in a study for the given [studyId].
     */
    fun getParticipantsForStudy( studyId: UUID ): List<Participant>
}