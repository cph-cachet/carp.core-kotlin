package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.users.AccountRepository
import dk.cachet.carp.deployment.domain.users.InMemoryUserRepository
import dk.cachet.carp.deployment.domain.users.UserRepository


/**
 * Tests for [ParticipationServiceHost].
 */
class ParticipationServiceHostTest : ParticipationServiceTest()
{
    override fun createUserService(): Triple<ParticipationService, UserRepository, AccountRepository>
    {
        val repo = InMemoryUserRepository()
        val accountRepo = InMemoryAccountRepository()
        val service = ParticipationServiceHost( repo, accountRepo )

        return Triple( service, repo, accountRepo )
    }
}
