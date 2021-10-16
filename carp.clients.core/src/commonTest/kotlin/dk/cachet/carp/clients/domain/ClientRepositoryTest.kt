package dk.cachet.carp.clients.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


interface ClientRepositoryTest
{
    /**
     * Called for each test to create a repository to run tests on.
     */
    fun createRepository(): ClientRepository


    @Test
    fun deviceRegistration_is_initially_null() = runSuspendTest {
        val repo = createRepository()
        assertNull( repo.getDeviceRegistration() )
    }

    @Test
    fun addStudyRuntime_can_be_retrieved() = runSuspendTest {
        val repo = createRepository()

        val runtime = StudyRuntime( UUID.randomUUID(), "Device role" )
        repo.addStudyRuntime( runtime )

        // Runtime can be retrieved by ID.
        val deploymentId = runtime.studyDeploymentId
        val roleName = runtime.deviceRoleName
        val retrievedRuntime = repo.getStudyRuntimeBy( deploymentId, roleName )
        assertNotNull( retrievedRuntime )

        // Runtime is included in list.
        val allRuntimes = repo.getStudyRuntimeList()
        assertEquals( 1, allRuntimes.count() )
        assertNotNull( allRuntimes.single { it.studyDeploymentId == deploymentId && it.deviceRoleName == roleName } )
    }

    @Test
    fun addStudyRuntime_fails_for_existing_runtime() = runSuspendTest {
        val repo = createRepository()
        val runtime = StudyRuntime( UUID.randomUUID(), "Device role" )
        repo.addStudyRuntime( runtime )

        assertFailsWith<IllegalArgumentException> { repo.addStudyRuntime( runtime ) }
    }

    @Test
    fun getStudyRuntimeBy_is_null_for_unknown_runtime() = runSuspendTest {
        val repo = createRepository()

        val unknownId = UUID.randomUUID()
        assertNull( repo.getStudyRuntimeBy( unknownId, "Unknown" ) )
    }

    @Test
    fun getStudyRuntimeList_is_empty_initially() = runSuspendTest {
        val repo = createRepository()

        assertEquals( 0, repo.getStudyRuntimeList().count() )
    }

    @Test
    fun updateStudyRuntime_succeeds() = runSuspendTest {
        val repo = createRepository()
        val deploymentId = UUID.randomUUID()
        val deviceRoleName = "Device role"
        val runtime = StudyRuntime( deploymentId, deviceRoleName )
        repo.addStudyRuntime( runtime )

        // Make some changes and update.
        val masterDevice = StubMasterDeviceDescriptor( deviceRoleName )
        val registration = masterDevice.createRegistration()
        val masterDeviceDeployment = MasterDeviceDeployment( StubMasterDeviceDescriptor( deviceRoleName ), registration )
        runtime.deploymentReceived( masterDeviceDeployment, emptySet() )
        runtime.completeDeployment( createDataListener() )
        repo.updateStudyRuntime( runtime )

        // Verify whether changes were stored.
        val retrievedRuntime = repo.getStudyRuntimeBy( deploymentId, deviceRoleName )
        assertNotNull( retrievedRuntime )
        assertEquals( runtime.getSnapshot(), retrievedRuntime.getSnapshot() )
    }

    @Test
    fun updateStudyRuntime_fails_for_unknown_runtime() = runSuspendTest {
        val repo = createRepository()

        val runtime = StudyRuntime( UUID.randomUUID(), "Device role" )
        assertFailsWith<IllegalArgumentException> { repo.updateStudyRuntime( runtime ) }
    }

    @Test
    fun removeStudyRuntime_succeeds() = runSuspendTest {
        val repo = createRepository()
        val runtime = StudyRuntime( UUID.randomUUID(), "Device role" )
        repo.addStudyRuntime( runtime )

        repo.removeStudyRuntime( runtime )

        val deploymentId = runtime.studyDeploymentId
        val roleName = runtime.deviceRoleName
        assertNull( repo.getStudyRuntimeBy( deploymentId, roleName ) )
        assertEquals( 0, repo.getStudyRuntimeList().count() )
    }

    @Test
    fun removeStudyRuntime_succeeds_when_runtime_not_present() = runSuspendTest {
        val repo = createRepository()
        val runtime = StudyRuntime( UUID.randomUUID(), "Device role" )
        repo.removeStudyRuntime( runtime )
    }
}
