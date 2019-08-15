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
    fun adding_study_deployment_and_retrieving_it_succeeds()
    {
        val repo = createDeploymentRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        repo.add( deployment )
        val retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertEquals( deployment.getSnapshot(), retrieved.getSnapshot() ) // StudyDeployment does not implement equals, but snapshot does.
    }

    @Test
    fun update_study_deployment_succeeds()
    {
        val repo = createDeploymentRepository()
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        repo.add( deployment )

        deployment.registerDevice( protocol.masterDevices.first(), DefaultDeviceRegistration( "0" ) )
        repo.update( deployment )
        val retrieved = repo.getStudyDeploymentBy( deployment.id )
        assertEquals( deployment.getSnapshot(), retrieved.getSnapshot() ) // StudyDeployment does not implement equals, but snapshot does.
    }
}