package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.ddd.EventBus
import dk.cachet.carp.common.ddd.SingleThreadedEventBus
import dk.cachet.carp.common.ddd.createApplicationServiceAdapter
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployment.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests whether different application service hosts in the deployments subsystem
 * are correctly integrated through integration events.
 */
class HostsIntegrationTest
{
    private lateinit var eventBus: EventBus

    private lateinit var accountService: AccountService
    private lateinit var deploymentService: DeploymentService
    private lateinit var participationService: ParticipationService

    @BeforeTest
    fun startServices()
    {
        eventBus = SingleThreadedEventBus()

        // Create deployment service.
        deploymentService = DeploymentServiceHost(
            InMemoryDeploymentRepository(),
            eventBus.createApplicationServiceAdapter( DeploymentService::class ) )

        // Create participation service.
        accountService = InMemoryAccountService()
        participationService = ParticipationServiceHost(
            InMemoryParticipationRepository(),
            accountService,
            eventBus.createApplicationServiceAdapter( ParticipationService::class ) )
    }

    @Test
    fun create_deployment_creates_participant_group() = runSuspendTest {
        var deploymentCreated: DeploymentService.Event.StudyDeploymentCreated? = null
        eventBus.registerHandler( DeploymentService.Event.StudyDeploymentCreated::class, this )
        {
            deploymentCreated = it
        }
        eventBus.activateHandlers( this )

        val protocol = createComplexProtocol().getSnapshot()
        val deployment = deploymentService.createStudyDeployment( protocol )
        val participantGroupData = participationService.getParticipantData( deployment.studyDeploymentId )

        assertEquals( deployment.studyDeploymentId, deploymentCreated?.deployment?.studyDeploymentId )
        assertEquals( protocol.expectedParticipantData.size, participantGroupData.data.size )
    }

    @Test
    fun removing_deployment_removes_participant_group() = runSuspendTest {
        var deploymentsRemoved: DeploymentService.Event.StudyDeploymentsRemoved? = null
        eventBus.registerHandler( DeploymentService.Event.StudyDeploymentsRemoved::class, this )
        {
            deploymentsRemoved = it
        }
        eventBus.activateHandlers( this )

        val protocol = createComplexProtocol().getSnapshot()
        val deployment = deploymentService.createStudyDeployment( protocol )

        deploymentService.removeStudyDeployments( setOf( deployment.studyDeploymentId ) )
        assertEquals( setOf( deployment.studyDeploymentId ), deploymentsRemoved?.deploymentIds )
        assertFailsWith<IllegalArgumentException>
        {
            participationService.getParticipantData( deployment.studyDeploymentId )
        }
    }

    @Test
    fun stopping_deployment_stops_participant_group() = runSuspendTest {
        var studyDeploymentStopped: DeploymentService.Event.StudyDeploymentStopped? = null
        eventBus.registerHandler( DeploymentService.Event.StudyDeploymentStopped::class, this )
        {
            studyDeploymentStopped = it
        }
        eventBus.activateHandlers( this )

        val protocol = createComplexProtocol().getSnapshot()
        val deployment = deploymentService.createStudyDeployment( protocol )
        val deploymentId = deployment.studyDeploymentId
        deploymentService.stop( deploymentId )

        assertEquals( deploymentId, studyDeploymentStopped?.studyDeploymentId )
        val deviceRoles = setOf( protocol.masterDevices.first().roleName )
        assertFailsWith<IllegalStateException> // Can no longer add participations once deployment stopped.
        {
            participationService.addParticipation(
                deploymentId,
                deviceRoles,
                AccountIdentity.fromUsername( "Test" ),
                StudyInvitation.empty()
            )
        }
    }

    @Test
    fun registration_changes_in_deployment_are_passed_to_participant_group() = runSuspendTest {
        // Create a deployment.
        val protocol = createComplexProtocol().getSnapshot()
        val deployment = deploymentService.createStudyDeployment( protocol )
        val deploymentId = deployment.studyDeploymentId

        // Add a participation for a new account with an assigned device.
        val accountId = AccountIdentity.fromEmailAddress( "test@test.com" )
        val assignedDevice = protocol.masterDevices.first()
        participationService.addParticipation(
            deploymentId,
            setOf( assignedDevice.roleName ),
            accountId,
            StudyInvitation.empty() )
        val account = accountService.findAccount( accountId )
        checkNotNull( account )

        // Subscribe to registration changes to test whether integration events are sent.
        var registrationChanged: DeploymentService.Event.DeviceRegistrationChanged? = null
        eventBus.registerHandler( DeploymentService.Event.DeviceRegistrationChanged::class, this )
        {
            registrationChanged = it
        }
        eventBus.activateHandlers( this )

        // Change registration for the assigned device.
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
}
