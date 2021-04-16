package dk.cachet.carp.clients.infrastructure

import dk.cachet.carp.clients.domain.ClientRepository
import dk.cachet.carp.clients.domain.ClientRepositoryTest


/**
 * Tests whether [InMemoryClientRepository] is implemented correctly.
 */
class InMemoryClientRepositoryTest : ClientRepositoryTest
{
    override fun createRepository(): ClientRepository = InMemoryClientRepository()
}
