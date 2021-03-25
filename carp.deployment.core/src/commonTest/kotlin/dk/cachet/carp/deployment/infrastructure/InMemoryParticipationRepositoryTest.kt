package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.users.ParticipationRepository
import dk.cachet.carp.deployment.domain.users.ParticipationRepositoryTest


/**
 * Tests whether [InMemoryDeploymentRepository] is implemented correctly.
 */
class InMemoryParticipationRepositoryTest : ParticipationRepositoryTest
{
    override fun createRepository(): ParticipationRepository = InMemoryParticipationRepository()
}
