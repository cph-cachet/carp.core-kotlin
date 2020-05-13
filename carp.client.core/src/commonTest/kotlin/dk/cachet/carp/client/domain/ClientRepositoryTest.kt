package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


interface ClientRepositoryTest
{
    /**
     * Called for each test to create a repository to run tests on.
     * You can use the [deploymentService] as a stub to initialize your repository.
     */
    fun createRepository( deploymentService: DeploymentService ): ClientRepository

    private fun createRepository(): Pair<ClientRepository, DeploymentService>
    {
        val deploymentService = DeploymentServiceHost( InMemoryDeploymentRepository(), InMemoryAccountService() )
        return Pair( createRepository( deploymentService ), deploymentService )
    }

    private suspend fun addTestDeployment( deploymentService: DeploymentService ): UUID
    {
        val protocol = createSmartphoneStudy()
        val snapshot = protocol.getSnapshot()
        val status = deploymentService.createStudyDeployment( snapshot )

        return status.studyDeploymentId
    }


    @Test
    fun deviceRegistration_is_initially_null()
    {
        val (repo, _) = createRepository()
        assertNull( repo.deviceRegistration )
    }

    @Test
    fun addStudyRuntime_can_be_retrieved() = runBlockingTest {
        val (repo, deploymentService) = createRepository()
        val deploymentId = addTestDeployment( deploymentService )
        val roleName = smartphone.roleName
        val studyRuntime =
            StudyRuntime.initialize( deploymentService, deploymentId, roleName, smartphone.createRegistration() )

        repo.addStudyRuntime( studyRuntime )

        // Runtime can be retrieved by ID.
        val retrievedRuntime = repo.getStudyRuntimeBy( deploymentId, roleName )
        assertNotNull( retrievedRuntime )

        // Runtime is included in list.
        val allRuntimes = repo.getStudyRuntimeList()
        assertEquals( 1, allRuntimes.count() )
        assertNotNull( allRuntimes.single { it.studyDeploymentId == deploymentId && it.device.roleName == roleName } )
    }

    @Test
    fun addStudyRuntime_fails_for_existing_runtime() = runBlockingTest {
        val (repo, deploymentService) = createRepository()
        val deploymentId = addTestDeployment( deploymentService )
        val roleName = smartphone.roleName
        val studyRuntime =
                StudyRuntime.initialize( deploymentService, deploymentId, roleName, smartphone.createRegistration() )
        repo.addStudyRuntime( studyRuntime )

        assertFailsWith<IllegalArgumentException> { repo.addStudyRuntime( studyRuntime ) }
    }

    @Test
    fun getStudyRuntimeBy_is_null_for_unknown_runtime()
    {
        val (repo, _) = createRepository()

        val unknownId = UUID.randomUUID()
        assertNull( repo.getStudyRuntimeBy( unknownId, "Unknown" ) )
    }

    @Test
    fun getStudyRuntimeList_is_empty_initially()
    {
        val (repo, _) = createRepository()

        assertEquals( 0, repo.getStudyRuntimeList().count() )
    }
}
