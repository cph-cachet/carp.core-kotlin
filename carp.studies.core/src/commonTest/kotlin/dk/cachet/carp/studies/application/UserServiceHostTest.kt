package dk.cachet.carp.studies.application

import dk.cachet.carp.studies.domain.NotifyUserServiceMock
import dk.cachet.carp.studies.domain.users.*


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
