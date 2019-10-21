package dk.cachet.carp.studies.application

import dk.cachet.carp.common.*
import dk.cachet.carp.studies.domain.*


/**
 * Application service which allows creating [Participant]'s for a [Study] and invites them to register in case they have not done so.
 */
interface ParticipantService
{
    /**
     * Create a participant for the [Study] with the specified [studyId] and identified by [emailAddress].
     */
    suspend fun createParticipant( studyId: UUID, emailAddress: EmailAddress ): Participant
}