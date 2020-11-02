package dk.cachet.carp.studies.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [ParticipantService].
 */
interface ParticipantServiceTest
{
    /**
     * Create a participant service and study service it depends on to be used in the tests.
     */
    fun createService(): Pair<ParticipantService, StudyService>


    @Test
    fun adding_and_retrieving_participant_succeeds() = runBlockingTest {
        val (participantService, studyService) = createService()
        val study = studyService.createStudy( StudyOwner(), "Test" )
        val studyId = study.studyId

        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val studyParticipants = participantService.getParticipants( studyId )
        assertEquals( participant, studyParticipants.single() )
    }

    @Test
    fun addParticipant_fails_for_unknown_studyId() = runBlockingTest {
        val (participantService, _) = createService()

        val email = EmailAddress( "test@test.com" )
        assertFailsWith<IllegalArgumentException> { participantService.addParticipant( unknownId, email ) }
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipant_twice_returns_same_participant() = runBlockingTest {
        val (participantService, studyService) = createService()
        val study = studyService.createStudy( StudyOwner(), "Test" )
        val studyId = study.studyId

        val email = EmailAddress( "test@test.com" )
        val p1 = participantService.addParticipant( studyId, email )
        val p2 = participantService.addParticipant( studyId, email )
        assertTrue( p1 == p2 )
    }

    @Test
    fun getParticipants_fails_for_unknown_studyId() = runBlockingTest {
        val (participantService, _) = createService()

        assertFailsWith<IllegalArgumentException> { participantService.getParticipants( unknownId ) }
    }

    @Test
    fun deployParticipantGroup_succeeds() = runBlockingTest {
        val (participantService, studyService) = createService()
        val (studyId, protocolSnapshot) = createLiveStudy( studyService )
        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        val assignParticipant = AssignParticipantDevices( participant.id, deviceRoles )
        val groupStatus = participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )
        assertEquals( participant.id, groupStatus.participants.single().participantId )
        val participantGroups = participantService.getParticipantGroupStatusList( studyId )
        val participantIdInGroup = participantGroups.single().participants.single().participantId
        assertEquals( participant.id, participantIdInGroup )
    }

    @Test
    fun deployParticipantGroup_fails_for_unknown_studyId() = runBlockingTest {
        val (participantService, _) = createService()
        val assignParticipant = AssignParticipantDevices( UUID.randomUUID(), setOf( "Test device" ) )

        assertFailsWith<IllegalArgumentException>
        {
            participantService.deployParticipantGroup( unknownId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun deployParticipantGroup_fails_for_empty_group() = runBlockingTest {
        val (participantService, studyService) = createService()
        val (studyId, _) = createLiveStudy( studyService )

        assertFailsWith<IllegalArgumentException> { participantService.deployParticipantGroup( studyId, setOf() ) }
    }

    @Test
    fun deployParticipantGroup_fails_for_unknown_participants() = runBlockingTest {
        val (participantService, studyService) = createService()
        val (studyId, protocolSnapshot) = createLiveStudy( studyService )

        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        val assignParticipant = AssignParticipantDevices( unknownId, deviceRoles )
        assertFailsWith<IllegalArgumentException>
        {
            participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun deployParticipantGroup_fails_for_unknown_device_roles() = runBlockingTest {
        val (participantService, studyService) = createService()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignParticipantDevices( participant.id, setOf( "Unknown device" ) )
        assertFailsWith<IllegalArgumentException>
        {
            participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun deployParticipantGroup_fails_when_not_all_devices_assigned() = runBlockingTest {
        val (participantService, studyService) = createService()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignParticipantDevices( participant.id, setOf() )
        assertFailsWith<IllegalArgumentException>
        {
            participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun deployParticipantGroup_multiple_times_returns_same_group() = runBlockingTest {
        val (participantService, studyService) = createService()
        val (studyId, protocolSnapshot) = createLiveStudy( studyService )
        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        val assignParticipant = AssignParticipantDevices( participant.id, deviceRoles )
        val groupStatus = participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )

        // Deploy the same group a second time.
        val groupStatus2 = participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )
        assertEquals( groupStatus, groupStatus2 )
    }

    @Test
    fun deployParticipantGroup_for_previously_stopped_group_returns_new_group() = runBlockingTest {
        val (participantService, studyService) = createService()
        val (studyId, protocolSnapshot) = createLiveStudy( studyService )
        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        val assignParticipant = AssignParticipantDevices( participant.id, deviceRoles )
        val groupStatus = participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )

        // Stop previous group. A new deployment with the same participants should be a new participant group.
        participantService.stopParticipantGroup( studyId, groupStatus.id )
        val groupStatus2 = participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )
        assertNotEquals( groupStatus, groupStatus2 )
    }

    @Test
    fun getParticipantGroupStatusList_returns_multiple_deployments() = runBlockingTest {
        val (participantService, studyService) = createService()
        val (studyId, protocolSnapshot) = createLiveStudy( studyService )
        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()

        val p1 = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignedP1 = AssignParticipantDevices( p1.id, deviceRoles )
        participantService.deployParticipantGroup( studyId, setOf( assignedP1 ) )

        val p2 = participantService.addParticipant( studyId, EmailAddress( "test2@test.com" ) )
        val assignedP2 = AssignParticipantDevices( p2.id, deviceRoles )
        participantService.deployParticipantGroup( studyId, setOf( assignedP2 ) )

        val participantIds = participantService.getParticipantGroupStatusList( studyId )
            .map { it.participants.single().participantId }
            .toSet()
        assertEquals( setOf( p1.id, p2.id ), participantIds )
    }

    @Test
    fun getParticipantGroupStatusLists_fails_for_unknown_studyId() = runBlockingTest {
        val (participantService, _) = createService()

        assertFailsWith<IllegalArgumentException> { participantService.getParticipantGroupStatusList( unknownId ) }
    }

    @Test
    fun stopParticipantGroup_succeeds() = runBlockingTest {
        val (participantService, studyService) = createService()
        val (studyId, protocolSnapshot) = createLiveStudy( studyService )
        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        val assignParticipant = AssignParticipantDevices( participant.id, deviceRoles )
        val groupStatus = participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )

        val stoppedGroupStatus = participantService.stopParticipantGroup( studyId, groupStatus.id )
        assertTrue( stoppedGroupStatus.studyDeploymentStatus is StudyDeploymentStatus.Stopped )
    }

    @Test
    fun stopParticipantGroup_fails_with_unknown_studyId() = runBlockingTest {
        val (participantService, _) = createService()

        assertFailsWith<IllegalArgumentException>
        {
            participantService.stopParticipantGroup( unknownId, UUID.randomUUID() )
        }
    }

    @Test
    fun stopParticipantGroup_fails_with_unknown_groupId() = runBlockingTest {
        val (participantService, studyService) = createService()
        val (studyId, _) = createLiveStudy( studyService )

        assertFailsWith<IllegalArgumentException> { participantService.stopParticipantGroup( studyId, unknownId ) }
    }


    private suspend fun createLiveStudy( service: StudyService ): Pair<UUID, StudyProtocolSnapshot>
    {
        // Create deployable protocol.
        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
        protocol.addMasterDevice( Smartphone( "User's phone" ) )
        val validSnapshot = protocol.getSnapshot()

        // Create live study from protocol.
        val status = service.createStudy( StudyOwner(), "Test" )
        val studyId = status.studyId
        service.setProtocol( studyId, validSnapshot )
        service.goLive( studyId )

        return Pair( studyId, validSnapshot )
    }
}
