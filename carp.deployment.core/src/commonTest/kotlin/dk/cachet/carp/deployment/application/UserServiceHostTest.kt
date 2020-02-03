package dk.cachet.carp.deployment.application

import dk.cachet.carp.deployment.domain.NotifyUserServiceMock
import dk.cachet.carp.deployment.domain.users.InMemoryUserRepository
import dk.cachet.carp.deployment.domain.users.UserRepository


/**
 * Tests for [UserServiceHost].
 */
class UserServiceHostTest : UserServiceTest()
{
    override fun createUserService( notify: NotifyUserServiceMock ): Pair<UserService, UserRepository>
    {
        val repo = InMemoryUserRepository()
        val service = UserServiceHost( repo, notify )

        return Pair( service, repo )
    }
}
