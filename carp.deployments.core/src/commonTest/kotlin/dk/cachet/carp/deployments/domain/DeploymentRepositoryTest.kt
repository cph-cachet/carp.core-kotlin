package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryWithConnectedDeviceProtocol
import kotlinx.coroutines.test.runTest
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
    fun adding_study_deployment_and_retrieving_it_succeeds() = runTest {
        val repo = createRepository()
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        repo.add( deployment )
        val retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertNotNull( retrieved )
        assertNotSame( deployment, retrieved ) // Should be new object instance.
        assertEquals( deployment.getSnapshot(), retrieved.getSnapshot() ) // StudyDeployment does not implement equals, but snapshot does.
    }

    @Test
    fun adding_study_deployment_with_existing_id_fails() = runTest {
        val repo = createRepository()
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        assertFailsWith<IllegalArgumentException>
        {
            repo.add( deployment )
        }
    }

    @Test
    fun getStudyDeploymentBy_succeeds() = runTest {
        val repo = createRepository()
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        val retrievedDeployment = repo.getStudyDeploymentBy( deployment.id )
        assertNotSame( deployment, retrievedDeployment ) // Should be new object instance.
        assertEquals( deployment.getSnapshot(), retrievedDeployment?.getSnapshot() )
    }

    @Test
    fun getStudyDeploymentBy_returns_null_for_unknown_id() = runTest {
        val repo = createRepository()

        val deployment = repo.getStudyDeploymentBy( unknownId )
        assertNull( deployment )
    }

    @Test
    fun getStudyDeploymentsBy_succeeds() = runTest {
        val repo = createRepository()
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment1 = studyDeploymentFor( protocol )
        val deployment2 = studyDeploymentFor( protocol )
        repo.add( deployment1 )
        repo.add( deployment2 )

        val ids = setOf( deployment1.id, deployment2.id )
        val retrievedDeployments = repo.getStudyDeploymentsBy( ids )
        assertEquals( 2, retrievedDeployments.count() )
        assertTrue( retrievedDeployments.map{ it.id }.containsAll( ids ) )
    }

    @Test
    fun getStudyDeploymentsBy_ignores_unknown_ids() = runTest {
        val repo = createRepository()
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        val ids = setOf( deployment.id, unknownId )
        val retrievedDeployments = repo.getStudyDeploymentsBy( ids )
        assertEquals( deployment.id, retrievedDeployments.single().id )
    }

    @Test
    fun update_study_deployment_succeeds() = runTest {
        val repo = createRepository()
        val (protocol, primaryDevice, connectedDevice) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        // Perform various actions on deployment, modifying it.
        // TODO: This does not verify whether registration history and invalidated devices are updated.
        with ( deployment )
        {
            registerDevice( primaryDevice, primaryDevice.createRegistration() )
            registerDevice( connectedDevice, connectedDevice.createRegistration() )

            val deviceDeployment = deployment.getDeviceDeploymentFor( primaryDevice )
            deviceDeployed( primaryDevice, deviceDeployment.lastUpdatedOn )

            stop()
        }

        // Update and verify whether retrieved deployment is the same.
        repo.update( deployment )
        val retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertEquals( deployment.getSnapshot(), retrieved?.getSnapshot() ) // StudyDeployment does not implement equals, but snapshot does.
    }

    @Test
    fun update_study_deployment_fails_for_unknown_deployment() = runTest {
        val repo = createRepository()
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        assertFailsWith<IllegalArgumentException>
        {
            repo.update( deployment )
        }
    }

    @Test
    fun remove_succeeds() = runTest {
        val repo = createRepository()
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment1 = studyDeploymentFor( protocol )
        val deployment2 = studyDeploymentFor( protocol )
        repo.add( deployment1 )
        repo.add( deployment2 )

        val deploymentIds = setOf( deployment1.id, deployment2.id )
        val removed = repo.remove( deploymentIds )
        assertEquals( deploymentIds, removed )
        val retrievedDeployment1 = repo.getStudyDeploymentBy( deployment1.id )
        val retrievedDeployment2 = repo.getStudyDeploymentBy( deployment2.id )
        assertNull( retrievedDeployment1 )
        assertNull( retrievedDeployment2 )
    }

    @Test
    fun remove_igores_unknown_ids() = runTest {
        val repo = createRepository()
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        val unknownId = UUID.randomUUID()
        val deploymentIds = setOf( deployment.id, unknownId )
        val removed = repo.remove( deploymentIds )
        assertEquals( setOf( deployment.id ), removed )
    }
}
