package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.*


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
     * Add [participant] information for a study that an account with the given [accountId] should participate in.
     *
     * @param accountId The ID of the account which acts as a [Participant] in a study.
     * @param participant The [Participant] information of the study to participate in.
     */
    fun addStudyParticipation( accountId: UUID, participant: Participant )

    /**
     * Get all participants included in a study for the given [studyId].
     */
    fun getParticipantsForStudy( studyId: UUID ): List<Participant>
}
