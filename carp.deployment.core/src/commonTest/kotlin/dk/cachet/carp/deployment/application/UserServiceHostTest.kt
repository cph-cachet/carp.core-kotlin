package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.users.AccountRepository
import dk.cachet.carp.deployment.domain.users.InMemoryUserRepository
import dk.cachet.carp.deployment.domain.users.UserRepository


/**
 * Tests for [UserServiceHost].
 */
class UserServiceHostTest : UserServiceTest()
{
    override fun createUserService(): Triple<UserService, UserRepository, AccountRepository>
    {
        val repo = InMemoryUserRepository()
        val accountRepo = InMemoryAccountRepository()
        val service = UserServiceHost( repo, accountRepo )

        return Triple( service, repo, accountRepo )
    }
}
