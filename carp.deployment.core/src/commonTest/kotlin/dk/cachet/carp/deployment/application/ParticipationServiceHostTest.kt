package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.users.AccountService
import dk.cachet.carp.common.users.InMemoryAccountService
import dk.cachet.carp.deployment.domain.users.InMemoryParticipationRepository


/**
 * Tests for [ParticipationServiceHost].
 */
class ParticipationServiceHostTest : ParticipationServiceTest()
{
    override fun createService(): Pair<ParticipationService, AccountService>
    {
        val repo = InMemoryParticipationRepository()
        val accountService = InMemoryAccountService()
        val participationService = ParticipationServiceHost( repo, accountService )

        return Pair( participationService, accountService )
    }
}
