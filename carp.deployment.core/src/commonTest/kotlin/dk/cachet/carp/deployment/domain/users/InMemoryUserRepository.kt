package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID


/**
 * A [UserRepository] which holds participations for study deployments in memory as long as the instance is held in memory.
 */
class InMemoryUserRepository : UserRepository
{
    private val participations: MutableMap<UUID, MutableSet<Participation>> = mutableMapOf()


    override fun addParticipation( accountId: UUID, participation: Participation )
    {
        val accountParticipations = participations.getOrPut( accountId ) { mutableSetOf() }
        accountParticipations.add( participation )
    }

    override fun getParticipations( accountId: UUID ): List<Participation> =
        participations[ accountId ]?.toList() ?: listOf()

    override fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation> =
        participations.flatMap { it.component2().filter { p -> p.studyDeploymentId == studyDeploymentId } }
}


/**
 * Tests whether the [InMemoryUserRepository] stub is implemented correctly.
 */
class InMemoryUserRepositoryTest : UserRepositoryTest
{
    override fun createUserRepository(): UserRepository = InMemoryUserRepository()
}
