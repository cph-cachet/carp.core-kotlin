package dk.cachet.carp.studies.application

import dk.cachet.carp.common.*
import dk.cachet.carp.studies.domain.*


/**
 * Application service which allows creating [Account]'s and including them as [Participant]'s for a [Study].
 */
interface UserService
{
    /**
     * Create a participant for the [Study] with the specified [studyId] and [Account] identified by [emailAddress].
     * In case no [Account] is associated with the specified [emailAddress] yet, an invitation to register is sent out.
     */
    suspend fun createParticipant( studyId: UUID, emailAddress: EmailAddress ): Participant
}