package dk.cachet.carp.studies.application

import dk.cachet.carp.common.*
import dk.cachet.carp.studies.domain.NotifyUserService
import dk.cachet.carp.studies.domain.users.*


/**
 * Implementation of [UserService] which allows creating [Account]'s and including them as [Participant]'s for a study.
 */
class UserServiceHost( private val repository: UserRepository, private val notifyUserService: NotifyUserService ) : UserService
{
    /**
     * Create an account which is identified by a unique [username].
     *
     * @throws IllegalArgumentException when an [Account] with the specified [username] already exists.
     */
    override suspend fun createAccount( username: Username ): Account
    {
        require( repository.findAccountWithIdentity( UsernameAccountIdentity( username ) ) == null )

        val account = Account.withUsernameIdentity( username )
        repository.addAccount( account )

        return account
    }

    /**
     * Create an account which is identified by an [emailAddress] someone has access to.
     * In case no [Account] is associated with the specified [emailAddress], send out a verification email.
     */
    override suspend fun createAccount( emailAddress: EmailAddress )
    {
        val existingAccount = repository.findAccountWithIdentity( EmailAccountIdentity( emailAddress ) )
        val isNewAccount = existingAccount == null

        if ( isNewAccount )
        {
            val newAccount = Account.withEmailIdentity( emailAddress )
            repository.addAccount( newAccount )
            notifyUserService.sendAccountVerificationEmail( newAccount.id, emailAddress )
        }
    }

    /**
     * Create a participant for the study with the specified [studyId] and [Account] identified by [accountId].
     *
     * @throws IllegalArgumentException when an [Account] with the specified [accountId] does not exist.
     */
    override suspend fun createParticipant( studyId: UUID, accountId: UUID ): Participant
    {
        require( repository.findAccountWithId( accountId ) != null )

        val participant = Participant( studyId )
        repository.addStudyParticipation( accountId, participant )

        return participant
    }

    /**
     * Create a participant for the study with the specified [studyId] and [Account] identified by [emailAddress].
     * In case no [Account] is associated with the specified [emailAddress] yet, an invitation to register is sent out.
     */
    override suspend fun inviteParticipant( studyId: UUID, emailAddress: EmailAddress ): Participant
    {
        var account = repository.findAccountWithIdentity( EmailAccountIdentity( emailAddress ) )
        val isNewAccount = account == null
        var participant: Participant? = account?.studyParticipations?.firstOrNull { it.studyId == studyId }

        // Create an unverified account if it does not yet exist and send out invitation email.
        if ( isNewAccount )
        {
            account = Account.withEmailIdentity( emailAddress )
            repository.addAccount( account )
            notifyUserService.sendAccountInvitationEmail( account.id, studyId, emailAddress )
        }

        // Create and add participant if it does not yet exist.
        if ( participant == null )
        {
            participant = Participant( studyId )
            repository.addStudyParticipation( account!!.id, participant )
        }

        return participant
    }
}