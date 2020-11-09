package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterWithConnectedDeviceProtocol
import dk.cachet.carp.test.runSuspendTest
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
    fun adding_study_deployment_and_retrieving_it_succeeds() = runSuspendTest {
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
    fun adding_study_deployment_with_existing_id_fails() = runSuspendTest {
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
    fun getStudyDeploymentBy_succeeds() = runSuspendTest {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        val retrievedDeployment = repo.getStudyDeploymentBy( deployment.id )
        assertNotSame( deployment, retrievedDeployment ) // Should be new object instance.
        assertEquals( deployment.getSnapshot(), retrievedDeployment?.getSnapshot() )
    }

    @Test
    fun getStudyDeploymentBy_returns_null_for_unknown_id() = runSuspendTest {
        val repo = createRepository()

        val deployment = repo.getStudyDeploymentBy( unknownId )
        assertNull( deployment )
    }

    @Test
    fun getStudyDeploymentsBy_succeeds() = runSuspendTest {
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
    fun getStudyDeploymentsBy_ignores_unknown_ids() = runSuspendTest {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        val ids = setOf( deployment.id, unknownId )
        val retrievedDeployments = repo.getStudyDeploymentsBy( ids )
        assertEquals( deployment.id, retrievedDeployments.single().id )
    }

    @Test
    fun update_study_deployment_succeeds() = runSuspendTest {
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
        val retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertEquals( deployment.getSnapshot(), retrieved?.getSnapshot() ) // StudyDeployment does not implement equals, but snapshot does.
    }

    @Test
    fun update_study_deployment_fails_for_unknown_deployment() = runSuspendTest {
        val repo = createRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        assertFailsWith<IllegalArgumentException>
        {
            repo.update( deployment )
        }
    }
}
