package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.deployments.domain.users.ParticipationRepository
import dk.cachet.carp.deployments.domain.users.ParticipationRepositoryTest


/**
 * Tests whether [InMemoryDeploymentRepository] is implemented correctly.
 */
class InMemoryParticipationRepositoryTest : ParticipationRepositoryTest
{
    override fun createRepository(): ParticipationRepository = InMemoryParticipationRepository()
}
