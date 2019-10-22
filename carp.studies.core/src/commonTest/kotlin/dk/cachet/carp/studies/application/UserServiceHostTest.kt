package dk.cachet.carp.studies.application

import dk.cachet.carp.studies.domain.InMemoryUserRepository


/**
 * Tests for [UserServiceHost].
 */
class UserServiceHostTest : UserServiceTest
{
    override fun createUserService(): UserService = UserServiceHost( InMemoryUserRepository() )
}