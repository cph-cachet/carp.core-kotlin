package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import kotlin.test.*


/**
 * Tests for implementations of [ParticipationRepository].
 */
interface ParticipationRepositoryTest
{
    fun createRepository(): ParticipationRepository


    @Test
    fun addStudyParticipation_and_retrieving_it_succeeds()
    {
        val repo = createRepository()
        val account = Account.withUsernameIdentity( "test" )
        val studyDeploymentId = UUID.randomUUID()
        val participation = Participation( studyDeploymentId )

        repo.addParticipation( account.id, participation )
        val participations = repo.getParticipationsForStudyDeployment( studyDeploymentId )

        assertEquals( participation, participations.single() )
    }

    @Test
    fun addStudyParticipation_with_existing_participation_only_adds_once()
    {
        val repo = createRepository()
        val account = Account.withUsernameIdentity( "test" )
        val studyDeploymentId = UUID.randomUUID()
        val participation = Participation( studyDeploymentId )

        repo.addParticipation( account.id, participation )
        repo.addParticipation( account.id, participation )
        val participations = repo.getParticipationsForStudyDeployment( studyDeploymentId )

        assertEquals( participation, participations.single() )
    }

    @Test
    fun getParticipationsForStudyDeployment_returns_matching_participations_only()
    {
        val repo = createRepository()
        val account = Account.withUsernameIdentity( "test" )
        val studyDeploymentId = UUID.randomUUID()
        val participations = listOf( Participation( studyDeploymentId ), Participation( studyDeploymentId ) )
        val otherParticipations = Participation( UUID.randomUUID() ) // Some other study deployment.

        (participations + otherParticipations).forEach { repo.addParticipation( account.id, it ) }
        val retrievedParticipations = repo.getParticipationsForStudyDeployment( studyDeploymentId )

        assertEquals( 2, retrievedParticipations.intersect( participations ).count() )
    }
}
