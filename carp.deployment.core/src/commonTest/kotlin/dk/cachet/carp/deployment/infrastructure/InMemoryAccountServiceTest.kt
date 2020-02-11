package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.domain.users.AccountServiceTest


/**
 * Tests whether [InMemoryAccountService] is implemented correctly.
 */
class InMemoryAccountServiceTest : AccountServiceTest()
{
    override fun createService(): AccountService = InMemoryAccountService()
}
