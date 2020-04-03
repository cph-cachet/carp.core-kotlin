package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
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
        val masterDevice = protocol.masterDevices.first()

        // Verify whether registering a device is updated.
        deployment.registerDevice( masterDevice, DefaultDeviceRegistration( "0" ) )
        repo.update( deployment )
        var retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertEquals( deployment.getSnapshot(), retrieved?.getSnapshot() ) // StudyDeployment does not implement equals, but snapshot does.

        // Verify whether deploying a device is updated.
        deployment.deviceDeployed( masterDevice )
        repo.update( deployment )
        retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertEquals( deployment.getSnapshot(), retrieved?.getSnapshot() )
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
    fun addInvitation_and_retrieving_it_succeeds()
    {
        val repo = createRepository()

        val account = Account.withUsernameIdentity( "test" )
        val participation = Participation( UUID.randomUUID() )
        val invitation = ParticipationInvitation( participation, StudyInvitation.empty(), setOf( "Test device" ) )
        repo.addInvitation( account.id, invitation )
        val retrievedInvitations = repo.getInvitations( account.id )
        assertEquals( invitation, retrievedInvitations.single() )
    }

    @Test
    fun getInvitations_is_empty_when_no_invitations()
    {
        val repo = createRepository()

        val invitations = repo.getInvitations( UUID.randomUUID() )
        assertEquals( 0, invitations.count() )
    }
}
