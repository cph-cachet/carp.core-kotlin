package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.Username
import dk.cachet.carp.common.users.UsernameAccountIdentity
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.test.Mock


class UserServiceMock(
    private val createAccountResult: Account = Account( UsernameAccountIdentity( "test" ) ),
    private val addParticipationResult: Participation = Participation( UUID.randomUUID() ),
    private val getParticipationsForStudyDeploymentResult: List<Participation> = listOf()
) : Mock<UserService>(), UserService
{
    @Suppress( "RemoveExplicitTypeArguments" ) // Compilation fails if `trackSuspendCall` type arguments are removed.
    override suspend fun createAccount( username: Username ): Account
    {
        trackSuspendCallOverloaded<Username, Account>( UserService::createAccount, "username", username )
        return createAccountResult
    }

    @Suppress( "RemoveExplicitTypeArguments" ) // Compilation fails if `trackSuspendCall` type arguments are removed.
    override suspend fun createAccount( emailAddress: EmailAddress ) =
        trackSuspendCallOverloaded<EmailAddress, Unit>( UserService::createAccount, "emailAddress", emailAddress )

    override suspend fun addParticipation( studyDeploymentId: UUID, identity: AccountIdentity ): Participation
    {
        trackSuspendCall( UserService::addParticipation, studyDeploymentId, identity )
        return addParticipationResult
    }

    override suspend fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation>
    {
        trackSuspendCall( UserService::getParticipationsForStudyDeployment, studyDeploymentId )
        return getParticipationsForStudyDeploymentResult
    }
}
