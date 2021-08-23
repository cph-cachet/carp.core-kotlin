package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.MutableDataStreamBatch
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.application.SyncPoint
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.createParticipantInvitation
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployments.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterDeviceProtocol
import dk.cachet.carp.test.runSuspendTest
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests whether different application service hosts relied upon by [DeploymentService]
 * are correctly integrated through integration events.
 */
class HostsIntegrationTest
{
    private lateinit var eventBus: EventBus

    private lateinit var accountService: AccountService
    private lateinit var deploymentService: DeploymentService
    private lateinit var participationService: ParticipationService
    private lateinit var dataStreamService: DataStreamService

    @BeforeTest
    fun startServices()
    {
        eventBus = SingleThreadedEventBus()

        dataStreamService = InMemoryDataStreamService()

        // Create deployment service.
        deploymentService = DeploymentServiceHost(
            InMemoryDeploymentRepository(),
            dataStreamService,
            eventBus.createApplicationServiceAdapter( DeploymentService::class ) )

        // Create participation service.
        accountService = InMemoryAccountService()
        participationService = ParticipationServiceHost(
            InMemoryParticipationRepository(),
            ParticipantGroupService( accountService ),
            eventBus.createApplicationServiceAdapter( ParticipationService::class ) )
    }

    @Test
    fun create_deployment_creates_participant_group() = runSuspendTest {
        var deploymentCreated: DeploymentService.Event.StudyDeploymentCreated? = null
        eventBus.registerHandler( DeploymentService::class, DeploymentService.Event.StudyDeploymentCreated::class, this )
        {
            deploymentCreated = it
        }
        eventBus.activateHandlers( this )

        val protocol = createComplexProtocol()
        val invitation = createParticipantInvitation( protocol )
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId, protocol.getSnapshot(), listOf( invitation ) )
        val participantGroupData = participationService.getParticipantData( deploymentId )

        assertEquals( deploymentId, deploymentCreated?.studyDeploymentId )
        assertEquals( protocol.expectedParticipantData.size, participantGroupData.data.size )
    }

    @Test
    fun create_deployment_adds_participations() = runSuspendTest {
        val deviceRole = "Phone"
        val protocol = createSingleMasterDeviceProtocol( deviceRole )

        // Create deployment with one invitation.
        val toInvite = EmailAccountIdentity( "test@test.com" )
        val assignedDevices = setOf( deviceRole )
        val studyInvitation = StudyInvitation.empty()
        val invitations = listOf(
            ParticipantInvitation( UUID.randomUUID(), assignedDevices, toInvite, studyInvitation )
        )
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId, protocol.getSnapshot(), invitations )

        // Verify whether the invitation was sent.
        val invitedAccount = accountService.findAccount( toInvite )
        assertNotNull( invitedAccount )
        val invitation = participationService.getActiveParticipationInvitations( invitedAccount.id ).singleOrNull()
        assertNotNull( invitation )
        assertEquals( deploymentId, invitation.participation.studyDeploymentId )
        assertEquals( assignedDevices, invitation.assignedDevices.map { it.device.roleName }.toSet() )
        assertEquals( studyInvitation, invitation.invitation )
    }

    @Test
    fun removing_deployment_removes_participant_group() = runSuspendTest {
        var deploymentsRemoved: DeploymentService.Event.StudyDeploymentsRemoved? = null
        eventBus.registerHandler( DeploymentService::class, DeploymentService.Event.StudyDeploymentsRemoved::class, this )
        {
            deploymentsRemoved = it
        }
        eventBus.activateHandlers( this )

        val protocol = createComplexProtocol()
        val invitation = createParticipantInvitation( protocol )
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId, protocol.getSnapshot(), listOf( invitation ) )

        deploymentService.removeStudyDeployments( setOf( deploymentId ) )
        assertEquals( setOf( deploymentId ), deploymentsRemoved?.deploymentIds )
        assertFailsWith<IllegalArgumentException>
        {
            participationService.getParticipantData( deploymentId )
        }
    }

    @Test
    fun stopping_deployment_stops_participant_group() = runSuspendTest {
        var studyDeploymentStopped: DeploymentService.Event.StudyDeploymentStopped? = null
        eventBus.registerHandler( DeploymentService::class, DeploymentService.Event.StudyDeploymentStopped::class, this )
        {
            studyDeploymentStopped = it
        }
        eventBus.activateHandlers( this )

        val protocol = createComplexProtocol()
        val invitation = createParticipantInvitation( protocol )
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId, protocol.getSnapshot(), listOf( invitation ) )
        deploymentService.stop( deploymentId )

        assertEquals( deploymentId, studyDeploymentStopped?.studyDeploymentId )
    }

    @Test
    fun registration_changes_in_deployment_are_passed_to_participant_group() = runSuspendTest {
        // Create a deployment.
        val protocol = createSingleMasterDeviceProtocol()
        val identity = AccountIdentity.fromUsername( "Test" )
        val invitation = createParticipantInvitation( protocol, identity )
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId, protocol.getSnapshot(), listOf( invitation ) )

        // Subscribe to registration changes to test whether integration events are sent.
        var registrationChanged: DeploymentService.Event.DeviceRegistrationChanged? = null
        eventBus.registerHandler( DeploymentService::class, DeploymentService.Event.DeviceRegistrationChanged::class, this )
        {
            registrationChanged = it
        }
        eventBus.activateHandlers( this )

        // Change registration for the assigned device.
        val assignedDevice = protocol.masterDevices.single()
        val account = accountService.findAccount( identity )!!
        val registration = assignedDevice.createRegistration()
        deploymentService.registerDevice( deploymentId, assignedDevice.roleName, registration )
        assertEquals( assignedDevice, registrationChanged?.device )
        assertEquals( registration, registrationChanged?.registration )
        var invitations = participationService.getActiveParticipationInvitations( account.id ).single()
        assertEquals( registration, invitations.assignedDevices.single().registration )

        // Remove registration for the assigned device.
        deploymentService.unregisterDevice( deploymentId, assignedDevice.roleName )
        assertEquals( assignedDevice, registrationChanged?.device )
        assertNull( registrationChanged?.registration )
        invitations = participationService.getActiveParticipationInvitations( account.id ).single()
        assertNull( invitations.assignedDevices.single().registration )
    }

    @Test
    fun appendToDataStreams_succeeds_as_long_as_deployment_is_ready() = runSuspendTest {
        // Create a protocol which when deployed has one `STUB_DATA_TYPE` data stream.
        val masterDevice = StubMasterDeviceDescriptor()
        val protocol = createEmptyProtocol()
        protocol.addMasterDevice( masterDevice )
        val task = StubTaskDescriptor( "Task", listOf( Measure.DataStream( STUB_DATA_TYPE ) ) )
        val atStartOfStudy = protocol.addTrigger( masterDevice.atStartOfStudy() )
        protocol.addTaskControl( atStartOfStudy.start( task, masterDevice ) )

        // Deploy protocol.
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment(
            deploymentId,
            protocol.getSnapshot(),
            listOf( createParticipantInvitation( protocol, AccountIdentity.fromUsername( "Test" ) ) )
        )

        // Prepare data to append to data stream.
        val stubStreamId = dataStreamId<StubData>( deploymentId, masterDevice.roleName )
        val syncPoint = SyncPoint( Clock.System.now() )
        val toAppend = MutableDataStreamBatch()
        toAppend.appendSequence(
            MutableDataStreamSequence( stubStreamId, 0, listOf( atStartOfStudy.id ), syncPoint )
                .apply { appendMeasurements( measurement( StubData(), 0 ) ) }
        )

        // Data streams aren't open yet since deployment is not ready.
        assertFailsWith<IllegalArgumentException> { dataStreamService.appendToDataStreams( deploymentId, toAppend ) }

        // Data can now be appended to data streams once the deployment is "ready".
        deploymentService.registerDevice( deploymentId, masterDevice.roleName, masterDevice.createRegistration() )
        val deviceDeployment = deploymentService.getDeviceDeploymentFor( deploymentId, masterDevice.roleName )
        deploymentService.deploymentSuccessful( deploymentId, masterDevice.roleName, deviceDeployment.lastUpdatedOn )
        dataStreamService.appendToDataStreams( deploymentId, toAppend )

        // Data can no longer be appended after a deployment is stopped.
        deploymentService.stop( deploymentId )
        val appendMore = MutableDataStreamBatch()
        appendMore.appendSequence(
            MutableDataStreamSequence( stubStreamId, 1, listOf( atStartOfStudy.id ), syncPoint )
                .apply { appendMeasurements( measurement( StubData(), 1000 ) ) }
        )
        assertFailsWith<IllegalStateException> { dataStreamService.appendToDataStreams( deploymentId, appendMore ) }
    }
}
