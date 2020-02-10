package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.users.AccountRepository
import dk.cachet.carp.deployment.domain.users.InMemoryParticipationRepository
import dk.cachet.carp.deployment.domain.users.ParticipationRepository


/**
 * Tests for [ParticipationServiceHost].
 */
class ParticipationServiceHostTest : ParticipationServiceTest()
{
    override fun createService(): Triple<ParticipationService, ParticipationRepository, AccountRepository>
    {
        val repo = InMemoryParticipationRepository()
        val accountRepo = InMemoryAccountRepository()
        val service = ParticipationServiceHost( repo, accountRepo )

        return Triple( service, repo, accountRepo )
    }
}
