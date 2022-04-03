package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.createParticipantInvitation
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryWithConnectedDeviceProtocol
import kotlinx.coroutines.test.runTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [DeploymentService].
 */
interface DeploymentServiceTest
{
    /**
     * Create a deployment service to be used in the tests.
     */
    fun createService(): DeploymentService


    @Test
    fun createStudyDeployment_registers_preregistered_devices() = runTest {
        val deploymentService = createService()
        val protocol = createSinglePrimaryWithConnectedDeviceProtocol()
        val primaryDevice = protocol.primaryDevices.single()
        val connectedDevice = protocol.getConnectedDevices( primaryDevice ).single()

        val deploymentId = UUID.randomUUID()
        val preregistration = connectedDevice.createRegistration()
        deploymentService.createStudyDeployment(
            deploymentId,
            protocol.getSnapshot(),
            listOf( createParticipantInvitation() ),
            mapOf( connectedDevice.roleName to preregistration )
        )
        deploymentService.registerDevice( deploymentId, primaryDevice.roleName, primaryDevice.createRegistration() )

        val deployment = deploymentService.getDeviceDeploymentFor( deploymentId, primaryDevice.roleName )
        assertEquals( preregistration, deployment.connectedDeviceRegistrations[ connectedDevice.roleName ] )
    }

    @Test
    fun createStudyDeployment_fails_for_existing_id() = runTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Primary" )

        val deviceRole = "Test device"
        val protocol = createSinglePrimaryDeviceProtocol( deviceRole )
        val invitation = ParticipantInvitation(
            UUID.randomUUID(),
            AssignedTo.Anyone,
            AccountIdentity.fromUsername( "User" ),
            StudyInvitation( "Some study" )
        )
        assertFailsWith<IllegalArgumentException> {
            deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )
        }
    }

    @Test
    fun removeStudyDeployments_succeeds() = runTest {
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
    fun removeStudyDeployments_ignores_unknown_ids() = runTest {
        val deploymentService = createService()
        val deploymentId = addTestDeployment( deploymentService, "Test device" )
        val unknownId = UUID.randomUUID()
        val deploymentIds = setOf( deploymentId, unknownId )

        val removedIds = deploymentService.removeStudyDeployments( deploymentIds )
        assertEquals( setOf( deploymentId ), removedIds )
    }

    @Test
    fun getStudyDeploymentStatus_succeeds() = runTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Test device" )

        // Actual testing of the status responses should already be covered adequately in StudyDeployment tests.
        deploymentService.getStudyDeploymentStatus( studyDeploymentId )
    }

    @Test
    fun getStudyDeploymentStatus_fails_for_unknown_studyDeploymentId() = runTest {
        val deploymentService = createService()

        assertFailsWith<IllegalArgumentException> { deploymentService.getStudyDeploymentStatus( unknownId ) }
    }

    @Test
    fun getStudyDeploymentStatusList_succeeds() = runTest {
        val deploymentService = createService()
        val deviceRoleName = "Primary"
        val protocol = createSinglePrimaryWithConnectedDeviceProtocol( deviceRoleName )
        val protocolSnapshot = protocol.getSnapshot()

        val invitation1 = createParticipantInvitation( AccountIdentity.fromUsername( "User 1" ) )
        val deploymentId1 = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId1, protocolSnapshot, listOf( invitation1 ) )
        val invitation2 = createParticipantInvitation( AccountIdentity.fromUsername( "User 2" ) )
        val deploymentId2 = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId2, protocolSnapshot, listOf( invitation2 ) )

        // Actual testing of the status responses should already be covered adequately in StudyDeployment tests.
        deploymentService.getStudyDeploymentStatusList( setOf( deploymentId1, deploymentId2 ) )
    }

    @Test
    fun getStudyDeploymentStatusList_fails_when_containing_an_unknown_studyDeploymentId() = runTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Test device" )

        val deploymentIds = setOf( studyDeploymentId, unknownId )
        assertFailsWith<IllegalArgumentException> { deploymentService.getStudyDeploymentStatusList( deploymentIds ) }
    }

    @Test
    fun registerDevice_can_be_called_multiple_times() = runTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Primary" )
        val status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val primary = status.getRemainingDevicesToRegister().first { it.roleName == "Primary" }

        val registration = primary.createRegistration()
        val firstRegisterStatus = deploymentService.registerDevice( studyDeploymentId, primary.roleName, registration )
        val secondRegisterStatus = deploymentService.registerDevice( studyDeploymentId, primary.roleName, registration )
        assertEquals( firstRegisterStatus, secondRegisterStatus )
    }

    @Test
    fun registerDevice_cannot_be_called_with_same_registration_when_stopped() = runTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Primary" )
        val status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val primary = status.getRemainingDevicesToRegister().first { it.roleName == "Primary" }
        val registration = primary.createRegistration()
        deploymentService.registerDevice( studyDeploymentId, primary.roleName, registration )
        deploymentService.stop( studyDeploymentId )

        assertFailsWith<IllegalStateException>
        {
            deploymentService.registerDevice( studyDeploymentId, primary.roleName, registration )
        }
    }

    @Test
    fun unregisterDevice_succeeds() = runTest {
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
    fun stop_succeeds() = runTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Test device" )

        val status = deploymentService.stop( studyDeploymentId )
        assertTrue( status is StudyDeploymentStatus.Stopped )
    }

    @Test
    fun stop_fails_for_unknown_studyDeploymentId() = runTest {
        val deploymentService = createService()

        assertFailsWith<IllegalArgumentException> { deploymentService.stop( unknownId ) }
    }

    @Test
    fun modifications_after_stop_not_allowed() = runTest {
        val deploymentService = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Primary", "Connected" )
        val status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val primary = status.getRemainingDevicesToRegister().first { it.roleName == "Primary" }
        val connected = status.getRemainingDevicesToRegister().first { it.roleName == "Connected" }
        deploymentService.registerDevice( studyDeploymentId, primary.roleName, primary.createRegistration() )
        deploymentService.registerDevice( studyDeploymentId, connected.roleName, connected.createRegistration() )
        deploymentService.stop( studyDeploymentId )

        assertFailsWith<IllegalStateException>
            { deploymentService.registerDevice( studyDeploymentId, connected.roleName, connected.createRegistration() ) }
        assertFailsWith<IllegalStateException>
            { deploymentService.unregisterDevice( studyDeploymentId, primary.roleName ) }
        val deviceDeployment = deploymentService.getDeviceDeploymentFor( studyDeploymentId, primary.roleName )
        assertFailsWith<IllegalStateException>
            { deploymentService.deviceDeployed( studyDeploymentId, primary.roleName, deviceDeployment.lastUpdatedOn ) }
    }


    /**
     * Create a deployment to be used in tests in the given [deploymentService] with a protocol
     * containing a single primary device with the specified [primaryDeviceRoleName]
     * and a connected device, of which the [connectedDeviceRoleName] can optionally be defined.
     */
    private suspend fun addTestDeployment(
        deploymentService: DeploymentService,
        primaryDeviceRoleName: String,
        connectedDeviceRoleName: String = "Connected"
    ): UUID
    {
        val protocol = createSinglePrimaryWithConnectedDeviceProtocol( primaryDeviceRoleName, connectedDeviceRoleName )
        val invitation = createParticipantInvitation()
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        return studyDeploymentId
    }
}
