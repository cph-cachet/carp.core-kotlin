package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.ddd.EventBus
import dk.cachet.carp.common.ddd.SingleThreadedEventBus
import dk.cachet.carp.common.ddd.createApplicationServiceAdapter
import dk.cachet.carp.common.users.AccountIdentity
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
    lateinit var eventBus: EventBus

    lateinit var deploymentService: DeploymentService
    lateinit var participationService: ParticipationService

    @BeforeTest
    fun startServices()
    {
        eventBus = SingleThreadedEventBus()

        // Create deployment service.
        val deploymentRepo = InMemoryDeploymentRepository()
        deploymentService = DeploymentServiceHost(
            deploymentRepo,
            eventBus.createApplicationServiceAdapter( DeploymentService::class ) )

        // Create participation service.
        val accountService = InMemoryAccountService()
        val participationRepository = InMemoryParticipationRepository()
        participationService = ParticipationServiceHost(
            deploymentRepo,
            participationRepository,
            accountService,
            eventBus.createApplicationServiceAdapter( ParticipationService::class ) )
    }

    @Test
    fun create_deployment_creates_participant_group() = runSuspendTest {
        var deploymentCreated: DeploymentService.Event.StudyDeploymentCreated? = null
        eventBus.subscribe( DeploymentService::class, DeploymentService.Event.StudyDeploymentCreated::class )
        {
            deploymentCreated = it
        }

        val protocol = createComplexProtocol().getSnapshot()
        val deployment = deploymentService.createStudyDeployment( protocol )
        val participantGroupData = participationService.getParticipantData( deployment.studyDeploymentId )

        assertEquals( deployment.studyDeploymentId, deploymentCreated?.deployment?.studyDeploymentId )
        assertEquals( protocol.expectedParticipantData.size, participantGroupData.data.size )
    }

    @Test
    fun stopping_deployment_stops_participant_group() = runSuspendTest {
        var studyDeploymentStopped: DeploymentService.Event.StudyDeploymentStopped? = null
        eventBus.subscribe( DeploymentService::class, DeploymentService.Event.StudyDeploymentStopped::class )
        {
            studyDeploymentStopped = it
        }

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
}
