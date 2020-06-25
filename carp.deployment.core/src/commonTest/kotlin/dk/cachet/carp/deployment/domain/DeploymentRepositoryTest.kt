package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterWithConnectedDeviceProtocol
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


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
    fun adding_study_deployment_and_retrieving_it_succeeds() = runBlockingTest {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        repo.add( deployment )
        val retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertNotNull( retrieved )
        assertNotSame( deployment, retrieved ) // Should be new object instance.
        assertEquals( deployment.getSnapshot(), retrieved.getSnapshot() ) // StudyDeployment does not implement equals, but snapshot does.
    }

    @Test
    fun adding_study_deployment_with_existing_id_fails() = runBlockingTest {
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
    fun getStudyDeploymentBy_succeeds() = runBlockingTest {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        val retrievedDeployment = repo.getStudyDeploymentBy( deployment.id )
        assertNotSame( deployment, retrievedDeployment ) // Should be new object instance.
        assertEquals( deployment.getSnapshot(), retrievedDeployment?.getSnapshot() )
    }

    @Test
    fun getStudyDeploymentBy_returns_null_for_unknown_id() = runBlockingTest {
        val repo = createRepository()

        val deployment = repo.getStudyDeploymentBy( unknownId )
        assertNull( deployment )
    }

    @Test
    fun getStudyDeploymentsBy_succeeds() = runBlockingTest {
        val repo = createRepository()
        val protocolSnapshot = createSingleMasterWithConnectedDeviceProtocol().getSnapshot()
        val deployment1 = StudyDeployment( protocolSnapshot )
        val deployment2 = StudyDeployment( protocolSnapshot )
        repo.add( deployment1 )
        repo.add( deployment2 )

        val ids = setOf( deployment1.id, deployment2.id )
        val retrievedDeployments = repo.getStudyDeploymentsBy( ids )
        assertEquals( 2, retrievedDeployments.count() )
        assertTrue( retrievedDeployments.map{ it.id }.containsAll( ids ) )
    }

    @Test
    fun getStudyDeploymentsBy_ignores_unknown_ids() = runBlockingTest {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        val ids = setOf( deployment.id, unknownId )
        val retrievedDeployments = repo.getStudyDeploymentsBy( ids )
        assertEquals( deployment.id, retrievedDeployments.single().id )
    }

    @Test
    fun update_study_deployment_succeeds() = runBlockingTest {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )
        val masterDevice = protocol.masterDevices.first()
        val connectedDevice = protocol.getConnectedDevices( masterDevice ).first()

        // Perform various actions on deployment, modifying it.
        // TODO: This does not verify whether registration history and invalidated devices are updated.
        with ( deployment )
        {
            registerDevice( masterDevice, masterDevice.createRegistration() )
            registerDevice( connectedDevice, connectedDevice.createRegistration() )

            val deviceDeployment = deployment.getDeviceDeploymentFor( masterDevice )
            deviceDeployed( masterDevice, deviceDeployment.lastUpdateDate )

            addParticipation( Account.withUsernameIdentity( "Test" ), Participation( deployment.id ) )

            stop()
        }

        // Update and verify whether retrieved deployment is the same.
        repo.update( deployment )
        var retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertEquals( deployment.getSnapshot(), retrieved?.getSnapshot() ) // StudyDeployment does not implement equals, but snapshot does.
    }

    @Test
    fun update_study_deployment_fails_for_unknown_deployment() = runBlockingTest {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        assertFailsWith<IllegalArgumentException>
        {
            repo.update( deployment )
        }
    }

    @Test
    fun addInvitation_and_retrieving_it_succeeds() = runBlockingTest {
        val repo = createRepository()

        val account = Account.withUsernameIdentity( "test" )
        val participation = Participation( UUID.randomUUID() )
        val invitation = ParticipationInvitation( participation, StudyInvitation.empty(), setOf( "Test device" ) )
        repo.addInvitation( account.id, invitation )
        val retrievedInvitations = repo.getInvitations( account.id )
        assertEquals( invitation, retrievedInvitations.single() )
    }

    @Test
    fun getInvitations_is_empty_when_no_invitations() = runBlockingTest {
        val repo = createRepository()

        val invitations = repo.getInvitations( UUID.randomUUID() )
        assertEquals( 0, invitations.count() )
    }
}
