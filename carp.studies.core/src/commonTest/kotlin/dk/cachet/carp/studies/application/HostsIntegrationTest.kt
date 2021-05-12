package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHost
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployments.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterDeviceProtocol
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import dk.cachet.carp.studies.application.users.StudyOwner
import dk.cachet.carp.studies.infrastructure.InMemoryParticipantRepository
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests whether different application service hosts are correctly integrated through integration events.
 */
class HostsIntegrationTest
{
    lateinit var eventBus: EventBus

    lateinit var studyService: StudyService
    lateinit var recruitmentService: RecruitmentService

    lateinit var deploymentService: DeploymentService
    lateinit var participationService: ParticipationService

    @BeforeTest
    fun startServices()
    {
        eventBus = SingleThreadedEventBus()

        // Create study service.
        val studyRepo = InMemoryStudyRepository()
        studyService = StudyServiceHost( studyRepo, eventBus.createApplicationServiceAdapter( StudyService::class ) )

        // Create deployment service.
        deploymentService = DeploymentServiceHost(
            InMemoryDeploymentRepository(),
            eventBus.createApplicationServiceAdapter( DeploymentService::class ) )

        // Create dependent participation service.
        val accountService = InMemoryAccountService()
        participationService = ParticipationServiceHost(
            InMemoryParticipationRepository(),
            ParticipantGroupService( accountService ),
            eventBus.createApplicationServiceAdapter( ParticipationService::class ) )

        recruitmentService = RecruitmentServiceHost(
            InMemoryParticipantRepository(),
            deploymentService,
            participationService,
            eventBus.createApplicationServiceAdapter( RecruitmentService::class ) )
    }


    @Test
    fun create_study_creates_recruitment() = runSuspendTest {
        var studyCreated: StudyService.Event.StudyCreated? = null
        eventBus.registerHandler( StudyService::class, StudyService.Event.StudyCreated::class, this ) { studyCreated = it }
        eventBus.activateHandlers( this )

        val study = studyService.createStudy( StudyOwner(), "Test" )
        val participants = recruitmentService.getParticipants( study.studyId )

        assertEquals( study.studyId, studyCreated?.study?.studyId )
        assertEquals( 0, participants.size )
    }

    @Test
    fun when_study_goes_live_recruitment_is_ready_for_deployment() = runSuspendTest {
        val study = studyService.createStudy( StudyOwner(), "Test" )
        val studyId = study.studyId
        val protocol = createSingleMasterDeviceProtocol( "Device" )
        studyService.setProtocol( studyId, protocol.getSnapshot() )

        var studyGoneLive: StudyService.Event.StudyGoneLive? = null
        eventBus.registerHandler( StudyService::class, StudyService.Event.StudyGoneLive::class, this ) { studyGoneLive = it }
        eventBus.activateHandlers( this )
        studyService.goLive( studyId )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        // Call succeeding means recruitment is ready for deployment.
        val assignDevices = setOf( AssignParticipantDevices( participant.id, setOf( "Device" ) ) )
        recruitmentService.deployParticipantGroup( study.studyId, assignDevices )

        assertEquals( study.studyId, studyGoneLive?.study?.studyId )
    }

    @Test
    fun remove_study_removes_recruitment_and_deployment() = runSuspendTest {
        val (studyId, deviceRole) = createLiveStudy()

        // Add participant and deploy participant group.
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignDevices = AssignParticipantDevices( participant.id, setOf( deviceRole ) )
        val group = recruitmentService.deployParticipantGroup( studyId, setOf( assignDevices ) )
        val deploymentId = group.studyDeploymentStatus.studyDeploymentId

        var studyRemovedEvent: StudyService.Event.StudyRemoved? = null
        eventBus.registerHandler( StudyService::class, StudyService.Event.StudyRemoved::class, this ) { studyRemovedEvent = it }
        var deploymentsRemovedEvent: DeploymentService.Event.StudyDeploymentsRemoved? = null
        eventBus.registerHandler( DeploymentService::class, DeploymentService.Event.StudyDeploymentsRemoved::class, this ) { deploymentsRemovedEvent = it }
        eventBus.activateHandlers( this )
        studyService.remove( studyId )

        assertEquals( studyId, studyRemovedEvent?.studyId )
        assertEquals( setOf( deploymentId ), deploymentsRemovedEvent?.deploymentIds )

         // Data related to study no longer exists.
        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipantGroupStatusList( studyId ) }
        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipants( studyId ) }
        assertFailsWith<IllegalArgumentException> { deploymentService.getStudyDeploymentStatus( deploymentId ) }
        assertFailsWith<IllegalArgumentException> { participationService.getParticipantData( deploymentId ) }
    }

    @Test
    fun remove_study_does_not_trigger_event_when_study_does_not_exist() = runSuspendTest {
        var removedEvent: StudyService.Event.StudyRemoved? = null
        eventBus.registerHandler( StudyService::class, StudyService.Event.StudyRemoved::class, this ) { removedEvent = it }
        eventBus.activateHandlers( this )
        studyService.remove( UUID.randomUUID() )

        assertNull( removedEvent )
    }


    /**
     * Create a live study with a protocol containing one device.
     */
    private suspend fun createLiveStudy(): Pair<UUID, String>
    {
        val study = studyService.createStudy( StudyOwner(), "Test" )
        val studyId = study.studyId
        val deviceRole = "Phone"
        val protocol = createSingleMasterDeviceProtocol( deviceRole )
        studyService.setProtocol( studyId, protocol.getSnapshot() )
        studyService.goLive( studyId )

        return Pair( studyId, deviceRole )
    }
}
