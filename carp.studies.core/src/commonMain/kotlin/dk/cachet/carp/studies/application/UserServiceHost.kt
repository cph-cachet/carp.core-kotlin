package dk.cachet.carp.studies.application

import dk.cachet.carp.common.*
import dk.cachet.carp.studies.domain.users.*


/**
 * Implementation of [UserService] which allows creating [Account]'s and including them as [Participant]'s for a [Study].
 */
class UserServiceHost( private val repository: UserRepository ) : UserService
{
    /**
     * Create a participant for the [Study] with the specified [studyId] and [Account] identified by [emailAddress].
     * In case no [Account] is associated with the specified [emailAddress] yet, an invitation to register is sent out.
     */
    override suspend fun createParticipant( studyId: UUID, emailAddress: EmailAddress ): Participant
    {
        var account = repository.findAccountWithEmail( emailAddress )
        var participant: Participant? = account?.studyParticipations?.firstOrNull { it.studyId == studyId }

        // Create an unverified account if it does not yet exist and send invitation email.
        if ( account == null )
        {
            account = Account( emailAddress )
            repository.addAccount( account )

            // TODO: Mark account as unverified and send out invitation email.
        }

        // Create and add participant if it does not yet exist.
        if ( participant == null )
        {
            participant = Participant( studyId )
            repository.addStudyParticipation( account.id, participant )
        }

        return participant
    }
}