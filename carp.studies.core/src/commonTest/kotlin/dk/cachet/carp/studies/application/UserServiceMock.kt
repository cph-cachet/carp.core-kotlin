package dk.cachet.carp.studies.application

import dk.cachet.carp.common.*
import dk.cachet.carp.studies.domain.users.*
import dk.cachet.carp.test.Mock


class UserServiceMock(
    private val createAccountResult: Account = Account( UsernameAccountIdentity( "test" ) ),
    private val createParticipantResult: Participant = Participant( UUID.randomUUID() ),
    private val inviteParticipantResult: Participant = Participant( UUID.randomUUID() ),
    private val getParticipantsForStudyResult: List<Participant> = listOf()
) : Mock<UserService>(), UserService
{
    @Suppress( "RemoveExplicitTypeArguments" ) // Compilation fails if `trackSuspendCall` type arguments are removed.
    override suspend fun createAccount( username: Username ): Account
    {
        trackSuspendCallOverloaded<Username, Account>( UserService::createAccount, "username", username )
        return createAccountResult
    }

    @Suppress( "RemoveExplicitTypeArguments" ) // Compilation fails if `trackSuspendCall` type arguments are removed.
    override suspend fun createAccount( emailAddress: EmailAddress )
        = trackSuspendCallOverloaded<EmailAddress, Unit>( UserService::createAccount, "emailAddress", emailAddress )

    override suspend fun createParticipant( studyId: UUID, accountId: UUID ): Participant
    {
        trackSuspendCall( UserService::createParticipant, studyId, accountId )
        return createParticipantResult
    }

    override suspend fun inviteParticipant( studyId: UUID, emailAddress: EmailAddress ): Participant
    {
        trackSuspendCall( UserService::inviteParticipant, studyId, emailAddress )
        return inviteParticipantResult
    }

    override suspend fun getParticipantsForStudy( studyId: UUID ): List<Participant>
    {
        trackSuspendCall( UserService::getParticipantsForStudy, studyId )
        return getParticipantsForStudyResult
    }
}