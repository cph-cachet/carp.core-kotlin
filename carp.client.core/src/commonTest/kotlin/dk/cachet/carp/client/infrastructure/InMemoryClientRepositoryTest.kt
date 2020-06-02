package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.domain.ClientRepository
import dk.cachet.carp.client.domain.ClientRepositoryTest


/**
 * Tests whether [InMemoryClientRepository] is implemented correctly.
 */
class InMemoryClientRepositoryTest : ClientRepositoryTest
{
    override fun createRepository(): ClientRepository = InMemoryClientRepository()
}
