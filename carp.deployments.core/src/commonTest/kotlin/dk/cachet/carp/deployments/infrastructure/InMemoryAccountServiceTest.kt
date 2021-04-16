package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.deployments.domain.users.AccountServiceTest


/**
 * Tests whether [InMemoryAccountService] is implemented correctly.
 */
class InMemoryAccountServiceTest : AccountServiceTest()
{
    override fun createService(): AccountService = InMemoryAccountService()
}
