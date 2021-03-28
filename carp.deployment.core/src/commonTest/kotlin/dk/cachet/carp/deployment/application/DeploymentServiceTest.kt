package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterWithConnectedDeviceProtocol
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [DeploymentService].
 */
abstract class DeploymentServiceTest
{
    /**
     * Create a deployment service to be used in the tests.
     */
    abstract fun createService(): DeploymentService


    @Test
    fun removeStudyDeployments_succeeds() = runSuspendTest {
        val deploymentService = createService()
        val deploymentId1 = addTestDeployment( deploymentService, "Test device" )
        val deploymentId2 = addTestDeployment( deploymentService, "Test device" )
        val deploymentIds = setOf( deploymentId1, deploymentId2 )

        val removedIds = deploymentService.removeStudyDeployments( deploymentIds )
        assertEquals( deploymentIds, removedIds )
        assertFailsWith<IllegalArgumentException> { deploymentService.getStudyDeploymentStatus( deploymentId1 ) }
        assertFailsWith<IllegalArgumentException> { deploymentService.getStudyDeploymentStatus( deploymentId2 ) }
    }

    @Test
    fun removeStudyDeployments_ignores_unknown_ids() = runSuspendTest {
        val deploymentService = createService()
        val deploymentId = addTestDeployment( deploymentService, "Test device" )
        val unknownId = UUID.randomUUID()
        val deploymentIds = setOf( deploymentId, unknownId )

        val removedIds = deploymentService.removeStudyDeployments( deploymentIds )
        assertEquals( setOf( deploymentId ), removedIds )
    }

    @Test
    fun getStudyDeploymentStatus_succeeds() = runSuspendTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Test device" )

        // Actual testing of the status responses should already be covered adequately in StudyDeployment tests.
        deploymentService.getStudyDeploymentStatus( studyDeploymentId )
    }

    @Test
    fun getStudyDeploymentStatus_fails_for_unknown_studyDeploymentId() = runSuspendTest {
        val deploymentService = createService()

        assertFailsWith<IllegalArgumentException> { deploymentService.getStudyDeploymentStatus( unknownId ) }
    }

    @Test
    fun getStudyDeploymentStatusList_succeeds() = runSuspendTest {
        val deploymentService = createService()
        val snapshot = createSingleMasterWithConnectedDeviceProtocol().getSnapshot()
        val status1 = deploymentService.createStudyDeployment( snapshot )
        val status2 = deploymentService.createStudyDeployment( snapshot )

        // Actual testing of the status responses should already be covered adequately in StudyDeployment tests.
        deploymentService.getStudyDeploymentStatusList( setOf( status1.studyDeploymentId, status2.studyDeploymentId ) )
    }

    @Test
    fun getStudyDeploymentStatusList_fails_when_containing_an_unknown_studyDeploymentId() = runSuspendTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Test device" )

        val deploymentIds = setOf( studyDeploymentId, unknownId )
        assertFailsWith<IllegalArgumentException> { deploymentService.getStudyDeploymentStatusList( deploymentIds ) }
    }

    @Test
    fun registerDevice_can_be_called_multiple_times() = runSuspendTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Master" )
        val status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val master = status.getRemainingDevicesToRegister().first { it.roleName == "Master" }

        val registration = master.createRegistration()
        val firstRegisterStatus = deploymentService.registerDevice( studyDeploymentId, master.roleName, registration )
        val secondRegisterStatus = deploymentService.registerDevice( studyDeploymentId, master.roleName, registration )
        assertEquals( firstRegisterStatus, secondRegisterStatus )
    }

    @Test
    fun registerDevice_cannot_be_called_with_same_registration_when_stopped() = runSuspendTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Master" )
        val status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val master = status.getRemainingDevicesToRegister().first { it.roleName == "Master" }
        val registration = master.createRegistration()
        deploymentService.registerDevice( studyDeploymentId, master.roleName, registration )
        deploymentService.stop( studyDeploymentId )

        assertFailsWith<IllegalStateException>
        {
            deploymentService.registerDevice( studyDeploymentId, master.roleName, registration )
        }
    }

    @Test
    fun unregisterDevice_succeeds() = runSuspendTest {
        val deploymentService = createService()
        val deviceRolename = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRolename )
        var status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val device = status.getRemainingDevicesToRegister().first { it.roleName == deviceRolename }
        deploymentService.registerDevice( studyDeploymentId, deviceRolename, device.createRegistration { } )

        status = deploymentService.unregisterDevice( studyDeploymentId, deviceRolename )
        assertTrue( device in status.getRemainingDevicesToRegister() )
    }

    @Test
    fun stop_succeeds() = runSuspendTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Test device" )

        val status = deploymentService.stop( studyDeploymentId )
        assertTrue( status is StudyDeploymentStatus.Stopped )
    }

    @Test
    fun stop_fails_for_unknown_studyDeploymentId() = runSuspendTest {
        val deploymentService = createService()

        assertFailsWith<IllegalArgumentException> { deploymentService.stop( unknownId ) }
    }

    @Test
    fun modifications_after_stop_not_allowed() = runSuspendTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Master", "Connected" )
        val status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val master = status.getRemainingDevicesToRegister().first { it.roleName == "Master" }
        val connected = status.getRemainingDevicesToRegister().first { it.roleName == "Connected" }
        deploymentService.registerDevice( studyDeploymentId, master.roleName, master.createRegistration() )
        deploymentService.registerDevice( studyDeploymentId, connected.roleName, connected.createRegistration() )
        deploymentService.stop( studyDeploymentId )

        assertFailsWith<IllegalStateException>
            { deploymentService.registerDevice( studyDeploymentId, connected.roleName, connected.createRegistration() ) }
        assertFailsWith<IllegalStateException>
            { deploymentService.unregisterDevice( studyDeploymentId, master.roleName ) }
        val deviceDeployment = deploymentService.getDeviceDeploymentFor( studyDeploymentId, master.roleName )
        assertFailsWith<IllegalStateException>
            { deploymentService.deploymentSuccessful( studyDeploymentId, master.roleName, deviceDeployment.lastUpdateDate ) }
    }


    /**
     * Create a deployment to be used in tests in the given [deploymentService] with a protocol
     * containing a single master device with the specified [masterDeviceRoleName]
     * and a connected device, of which the [connectedDeviceRoleName] can optionally be defined.
     */
    private suspend fun addTestDeployment(
        deploymentService: DeploymentService,
        masterDeviceRoleName: String,
        connectedDeviceRoleName: String = "Connected"
    ): UUID
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol( masterDeviceRoleName, connectedDeviceRoleName )
        val snapshot = protocol.getSnapshot()
        val status = deploymentService.createStudyDeployment( snapshot )

        return status.studyDeploymentId
    }
}
