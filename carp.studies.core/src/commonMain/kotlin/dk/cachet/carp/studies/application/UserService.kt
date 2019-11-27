package dk.cachet.carp.studies.application

import dk.cachet.carp.common.*
import dk.cachet.carp.studies.domain.users.*


/**
 * Application service which allows creating [Account]'s and including them as [Participant]'s for a study.
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
     * Create a participant for the study with the specified [studyId] and [Account] identified by [accountId].
     *
     * @throws IllegalArgumentException when an [Account] with the specified [accountId] does not exist.
     */
    suspend fun createParticipant( studyId: UUID, accountId: UUID ): Participant

    /**
     * Create a participant for the study with the specified [studyId] and [Account] identified by [emailAddress].
     * In case no [Account] is associated with the specified [emailAddress], send out an invitation to register in order to participate in the study.
     * TODO: studyId should be replaced with specific information about the study in order to prevent a dependency on study service here.
     */
    suspend fun inviteParticipant( studyId: UUID, emailAddress: EmailAddress ): Participant

    /**
     * Get all participants included in a study for the given [studyId].
     */
    suspend fun getParticipantsForStudy( studyId: UUID ): List<Participant>
}
