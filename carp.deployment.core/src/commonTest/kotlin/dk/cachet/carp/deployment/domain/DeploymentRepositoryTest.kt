package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import kotlin.test.*


/**
 * Tests for implementations of [DeploymentRepository].
 */
interface DeploymentRepositoryTest
{
    /**
     * Called for each test to create a repository to run tests on.
     */
    fun createRepository(): DeploymentRepository


    @Test
    fun adding_study_deployment_and_retrieving_it_succeeds()
    {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        repo.add( deployment )
        val retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertNotNull( retrieved )
        assertEquals( deployment.getSnapshot(), retrieved.getSnapshot() ) // StudyDeployment does not implement equals, but snapshot does.
    }

    @Test
    fun adding_study_deployment_with_existing_id_fails()
    {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        assertFailsWith<IllegalArgumentException>
        {
            repo.add( deployment )
        }
    }

    @Test
    fun getStudyDeploymentById_returns_null_for_unknown_id()
    {
        val repo = createRepository()

        val deployment = repo.getStudyDeploymentBy( UUID.randomUUID() )
        assertNull( deployment )
    }

    @Test
    fun update_study_deployment_succeeds()
    {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        deployment.registerDevice( protocol.masterDevices.first(), DefaultDeviceRegistration( "0" ) )
        repo.update( deployment )
        val retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertNotNull( retrieved )
        assertEquals( deployment.getSnapshot(), retrieved.getSnapshot() ) // StudyDeployment does not implement equals, but snapshot does.
    }

    @Test
    fun update_study_deployment_fails_for_unknown_deployment()
    {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        assertFailsWith<IllegalArgumentException>
        {
            repo.update( deployment )
        }
    }

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
