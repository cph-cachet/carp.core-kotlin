package dk.cachet.carp.studies.application

import dk.cachet.carp.common.*
import dk.cachet.carp.studies.domain.*


/**
 * Application service which allows creating users and including them as [Participant]'s for a [Study].
 */
interface UserService
{
    /**
     * Create a participant for the [Study] with the specified [studyId] and user identified by [emailAddress].
     */
    suspend fun createParticipant( studyId: UUID, emailAddress: EmailAddress ): Participant
}