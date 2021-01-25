package dk.cachet.carp.studies.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.EventBus
import dk.cachet.carp.common.ddd.SingleThreadedEventBus
import dk.cachet.carp.common.ddd.createApplicationServiceAdapter
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.application.ParticipationService
import dk.cachet.carp.deployment.application.ParticipationServiceHost
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployment.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterDeviceProtocol
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.StudyOwner
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
    lateinit var participantService: ParticipantService

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
        val deploymentRepo = InMemoryDeploymentRepository()
        deploymentService = DeploymentServiceHost( deploymentRepo )

        // Create dependent participation service.
        val accountService = InMemoryAccountService()
        val participationRepository = InMemoryParticipationRepository()
        participationService = ParticipationServiceHost( deploymentRepo, participationRepository, accountService )

        participantService = ParticipantServiceHost(
            InMemoryParticipantRepository(),
            deploymentService,
            participationService,
            eventBus.createApplicationServiceAdapter( ParticipantService::class ) )
    }


    @Test
    fun create_study_creates_recruitment() = runSuspendTest {
        var studyCreated: StudyService.Event.StudyCreated? = null
        eventBus.subscribe( StudyService::class, StudyService.Event.StudyCreated::class ) { studyCreated = it }

        val study = studyService.createStudy( StudyOwner(), "Test" )
        val participants = participantService.getParticipants( study.studyId )

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
        eventBus.subscribe( StudyService::class, StudyService.Event.StudyGoneLive::class ) { studyGoneLive = it }
        studyService.goLive( studyId )
        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        // Call succeeding means recruitment is ready for deployment.
        val assignDevices = setOf( AssignParticipantDevices( participant.id, setOf( "Device" ) ) )
        participantService.deployParticipantGroup( study.studyId, assignDevices )

        assertEquals( study.studyId, studyGoneLive?.study?.studyId )
    }

    @Test
    fun remove_study_removes_recruitment() = runSuspendTest {
        val owner = StudyOwner()
        val studyStatus = studyService.createStudy( owner, "Test" )
        val studyId = studyStatus.studyId
        participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        var removedEvent: StudyService.Event.StudyRemoved? = null
        eventBus.subscribe( StudyService::class, StudyService.Event.StudyRemoved::class ) { removedEvent = it }
        studyService.remove( studyId )

        assertEquals( studyId, removedEvent?.studyId )

         // Data related to study no longer exists.
        assertFailsWith<IllegalArgumentException> { participantService.getParticipantGroupStatusList( studyId ) }
        assertFailsWith<IllegalArgumentException> { participantService.getParticipants( studyId ) }
    }

    @Test
    fun remove_study_does_not_trigger_event_when_study_does_not_exist() = runSuspendTest {
        var removedEvent: StudyService.Event.StudyRemoved? = null
        eventBus.subscribe( StudyService::class, StudyService.Event.StudyRemoved::class ) { removedEvent = it }
        studyService.remove( UUID.randomUUID() )

        assertNull( removedEvent )
    }
}
