package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.studies.domain.users.ParticipantRepository
import dk.cachet.carp.studies.domain.users.ParticipantRepositoryTest


/**
 * Tests whether [InMemoryParticipantRepository] is implemented correctly.
 */
class InMemoryParticipantRepositoryTest : ParticipantRepositoryTest
{
    override fun createRepository(): ParticipantRepository = InMemoryParticipantRepository()
}
