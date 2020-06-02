package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.domain.createSingleMasterWithConnectedDeviceProtocol
import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [DeploymentService].
 */
abstract class DeploymentServiceTest
{
    /**
     * Create a deployment service and account service it depends on to be used in the tests.
     */
    abstract fun createService(): Pair<DeploymentService, AccountService>


    @Test
    fun getStudyDeploymentStatus_succeeds() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Test device" )

        // Actual testing of the status responses should already be covered adequately in StudyDeployment tests.
        deploymentService.getStudyDeploymentStatus( studyDeploymentId )
    }

    @Test
    fun getStudyDeploymentStatus_fails_for_unknown_studyDeploymentId() = runBlockingTest {
        val ( deploymentService, _ ) = createService()

        assertFailsWith<IllegalArgumentException> { deploymentService.getStudyDeploymentStatus( unknownId ) }
    }

    @Test
    fun getStudyDeploymentStatusList_succeeds() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val snapshot = createSingleMasterWithConnectedDeviceProtocol().getSnapshot()
        val status1 = deploymentService.createStudyDeployment( snapshot )
        val status2 = deploymentService.createStudyDeployment( snapshot )

        // Actual testing of the status responses should already be covered adequately in StudyDeployment tests.
        deploymentService.getStudyDeploymentStatusList( setOf( status1.studyDeploymentId, status2.studyDeploymentId ) )
    }

    @Test
    fun getStudyDeploymentStatusList_fails_when_containing_an_unknown_studyDeploymentId() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Test device" )

        val deploymentIds = setOf( studyDeploymentId, unknownId )
        assertFailsWith<IllegalArgumentException> { deploymentService.getStudyDeploymentStatusList( deploymentIds ) }
    }

    @Test
    fun registerDevice_can_be_called_multiple_times() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Master" )
        val status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val master = status.getRemainingDevicesToRegister().first { it.roleName == "Master" }

        val registration = master.createRegistration()
        val firstRegisterStatus = deploymentService.registerDevice( studyDeploymentId, master.roleName, registration )
        val secondRegisterStatus = deploymentService.registerDevice( studyDeploymentId, master.roleName, registration )
        assertEquals( firstRegisterStatus, secondRegisterStatus )
    }

    @Test
    fun registerDevice_cannot_be_called_with_same_registration_when_stopped() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
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
    fun unregisterDevice_succeeds() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val deviceRolename = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRolename )
        var status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val device = status.getRemainingDevicesToRegister().first { it.roleName == deviceRolename }
        deploymentService.registerDevice( studyDeploymentId, deviceRolename, device.createRegistration { } )

        status = deploymentService.unregisterDevice( studyDeploymentId, deviceRolename )
        assertTrue( device in status.getRemainingDevicesToRegister() )
    }

    @Test
    fun stop_succeeds() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Test device" )

        val status = deploymentService.stop( studyDeploymentId )
        assertTrue( status is StudyDeploymentStatus.Stopped )
    }

    @Test
    fun stop_fails_for_unknown_studyDeploymentId() = runBlockingTest {
        val ( deploymentService, _ ) = createService()

        assertFailsWith<IllegalArgumentException> { deploymentService.stop( unknownId ) }
    }

    @Test
    fun modifications_after_stop_not_allowed() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
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
            { deploymentService.deploymentSuccessful( studyDeploymentId, master.roleName, deviceDeployment.getChecksum() ) }
        val accountId = AccountIdentity.fromUsername( "Test" )
        val invitation = StudyInvitation.empty()
        assertFailsWith<IllegalStateException>
            { deploymentService.addParticipation( studyDeploymentId, setOf( "Master" ), accountId, invitation ) }
    }

    @Test
    fun addParticipation_has_matching_studyDeploymentId() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val deviceRoleName = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRoleName )

        val accountIdentity = AccountIdentity.fromUsername( "test" )
        val invitation = StudyInvitation.empty()
        val participation = deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), accountIdentity, invitation )

        assertEquals( studyDeploymentId, participation.studyDeploymentId )
    }

    @Test
    fun addParticipation_creates_new_account_for_new_identity() = runBlockingTest {
        val ( deploymentService, accountService ) = createService()
        val deviceRoleName = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRoleName )

        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()
        deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )

        // Verify whether account was added.
        val foundAccount = accountService.findAccount( emailIdentity )
        assertNotNull( foundAccount )
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipation_with_same_studyDeploymentId_and_identity() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val deviceRoleName = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRoleName )

        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()

        // Adding the same identity to a deployment returns the same participation.
        val p1: Participation = deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )
        val p2: Participation = deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )
        assertTrue( p1.id == p2.id )
    }

    @Test
    fun addParticipation_fails_for_second_differing_request() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val deviceRoleName = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRoleName )
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()
        deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, invitation )

        val differentInvitation = StudyInvitation( "Different", "New description" )
        assertFailsWith<IllegalStateException>
        {
            deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), emailIdentity, differentInvitation )
        }
    }

    @Test
    fun addParticipation_fails_for_unknown_studyDeploymentId() = runBlockingTest {
        val ( deploymentService, _ ) = createService()

        val identity = AccountIdentity.fromUsername( "test" )
        assertFailsWith<IllegalArgumentException>
        {
            deploymentService.addParticipation( unknownId, setOf( "Device" ), identity, StudyInvitation.empty() )
        }
    }

    @Test
    fun addParticipation_fails_for_unknown_deviceRoleNames() = runBlockingTest {
        val ( deploymentService, _ ) = createService()
        val studyDeploymentId = addTestDeployment( deploymentService, "Some device" )
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()

        assertFailsWith<IllegalArgumentException>
        {
            deploymentService.addParticipation( studyDeploymentId, setOf( "Wrong device" ), emailIdentity, invitation )
        }
    }

    @Test
    fun addParticipation_and_retrieving_invitation_succeeds() = runBlockingTest {
        val ( deploymentService, accountService ) = createService()
        val deviceRoleName = "Test device"
        val studyDeploymentId = addTestDeployment( deploymentService, deviceRoleName )
        val identity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation.empty()

        val participation = deploymentService.addParticipation( studyDeploymentId, setOf( deviceRoleName ), identity, invitation )
        val account = accountService.findAccount( identity )
        assertNotNull( account )
        val retrievedInvitations = deploymentService.getParticipationInvitations( account.id )
        assertEquals( ParticipationInvitation( participation, invitation, setOf( deviceRoleName ) ), retrievedInvitations.single() )
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
