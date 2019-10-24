package dk.cachet.carp.studies.application

import dk.cachet.carp.studies.domain.users.*


/**
 * Tests for [UserServiceHost].
 */
class UserServiceHostTest : UserServiceTest
{
    override fun createUserService(): Pair<UserService, UserRepository>
    {
        val repo = InMemoryUserRepository()
        val service = UserServiceHost( repo )

        return Pair( service, repo )
    }
}