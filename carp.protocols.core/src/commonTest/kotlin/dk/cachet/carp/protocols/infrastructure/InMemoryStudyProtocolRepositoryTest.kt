package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.protocols.domain.StudyProtocolRepository
import dk.cachet.carp.protocols.domain.StudyProtocolRepositoryTest


/**
 * Tests whether [InMemoryStudyProtocolRepository] is implemented correctly.
 */
class InMemoryStudyProtocolRepositoryTest : StudyProtocolRepositoryTest
{
    override fun createRepository(): StudyProtocolRepository = InMemoryStudyProtocolRepository()
}
