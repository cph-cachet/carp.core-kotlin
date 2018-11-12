package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import kotlin.test.*


/**
 * Tests for implementations of [DeploymentRepository].
 */
interface  DeploymentRepositoryTest
{
    /**
     * Called for each test to create a repository to run tests on.
     */
    fun createDeploymentRepository(): DeploymentRepository


    @Test
    fun adding_deployment_and_retrieving_it_succeeds()
    {
        val repo = createDeploymentRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = deploymentFor( protocol )

        repo.add( deployment )
        val retrieved = repo.getBy( deployment.id )
        assertEquals( deployment.getSnapshot(), retrieved.getSnapshot() ) // Deployment does not implement equals, but snapshot does.
    }

    @Test
    fun update_deployment_succeeds()
    {
        val repo = createDeploymentRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = deploymentFor( protocol )
        repo.add( deployment )

        deployment.registerDevice( protocol.masterDevices.first(), DefaultDeviceRegistration( "0" ) )
        repo.update( deployment )
        val retrieved = repo.getBy( deployment.id )
        assertEquals( deployment.getSnapshot(), retrieved.getSnapshot() ) // Deployment does not implement equals, but snapshot does.
    }
}