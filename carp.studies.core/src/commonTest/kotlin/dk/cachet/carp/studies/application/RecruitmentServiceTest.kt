package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [RecruitmentService].
 */
interface RecruitmentServiceTest
{
    /**
     * System under test: the [recruitmentService] and all dependencies to be used in tests.
     */
    data class SUT(
        val recruitmentService: RecruitmentService,
        val studyService: StudyService,
        val eventBus: EventBus
    )

    /**
     * Create the system under test (SUT): the [RecruitmentService] and all dependencies to be used in tests.
     */
    fun createSUT(): SUT


    @Test
    fun adding_and_retrieving_participants_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val study = studyService.createStudy( UUID.randomUUID(), "Test" )
        val studyId = study.studyId

        val emailParticipant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val usernameParticipant = recruitmentService.addParticipant( studyId, Username( "test" ) )

        // Get participants by ID.
        assertEquals( emailParticipant, recruitmentService.getParticipant( studyId, emailParticipant.id ) )
        assertEquals( usernameParticipant, recruitmentService.getParticipant( studyId, usernameParticipant.id ) )

        // Get all participants.
        val studyParticipants = recruitmentService.getParticipants( studyId )
        assertEquals( setOf( emailParticipant, usernameParticipant ), studyParticipants.toSet() )
    }

    @Test
    fun addParticipant_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()

        val email = EmailAddress( "test@test.com" )
        assertFailsWith<IllegalArgumentException> { recruitmentService.addParticipant( unknownId, email ) }
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipant_twice_returns_same_participant() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val study = studyService.createStudy( UUID.randomUUID(), "Test" )
        val studyId = study.studyId

        val email = EmailAddress( "test@test.com" )
        val p1 = recruitmentService.addParticipant( studyId, email )
        val p2 = recruitmentService.addParticipant( studyId, email )
        assertTrue( p1 == p2 )
    }

    @Test
    fun getParticipant_fails_for_unknown_id() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val study = studyService.createStudy( UUID.randomUUID(), "Test" )

        // Unknown study id.
        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipant( unknownId, unknownId ) }

        // Unknown participant id.
        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipant( study.studyId, unknownId ) }
    }

    @Test
    fun getParticipants_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()

        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipants( unknownId ) }
    }

    @Test
    fun inviteNewParticipantGroup_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        assertEquals( participant, groupStatus.participants.single() )
        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        val participantInGroup = participantGroups.single().participants.single()
        assertEquals( participant, participantInGroup )
    }

    @Test
    fun inviteNewParticipantGroup_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()
        val assignParticipant = AssignedParticipantRoles( UUID.randomUUID(), AssignedTo.All )

        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.inviteNewParticipantGroup( unknownId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun inviteNewParticipantGroup_fails_for_empty_group() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        assertFailsWith<IllegalArgumentException> { recruitmentService.inviteNewParticipantGroup( studyId, setOf() ) }
    }

    @Test
    fun inviteNewParticipantGroup_fails_for_unknown_participants() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        val assignParticipant = AssignedParticipantRoles( unknownId, AssignedTo.All )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun inviteNewParticipantGroup_fails_for_unknown_participant_roles() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.Roles( setOf( "Unknown role" ) ) )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun inviteNewParticipantGroup_fails_when_not_all_participant_roles_assigned() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, protocol) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val role = protocol.participantRoles.first().role
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.Roles( setOf( role ) ) )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun inviteNewParticipantGroup_multiple_times_returns_same_group() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )

        // Deploy the same group a second time.
        val groupStatus2 = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        assertEquals( groupStatus, groupStatus2 )
    }

    @Test
    fun inviteNewParticipantGroup_for_previously_stopped_group_returns_new_group() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )

        // Stop previous group. A new deployment with the same participants should be a new participant group.
        recruitmentService.stopParticipantGroup( studyId, groupStatus.id )
        val groupStatus2 = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        assertNotEquals( groupStatus, groupStatus2 )
    }

    @Test
    fun createParticipantGroup_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.createParticipantGroup( studyId, setOf( assignParticipant ) )
        assertEquals( participant, groupStatus.participants.single() )
        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        val participantInGroup = participantGroups.single().participants.single()
        assertEquals( participant, participantInGroup )
    }

    @Test
    fun createParticipantGroup_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()
        val assignParticipant = AssignedParticipantRoles( UUID.randomUUID(), AssignedTo.All )

        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.createParticipantGroup( unknownId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun createParticipantGroup_fails_for_unknown_participants() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        val assignParticipant = AssignedParticipantRoles( unknownId, AssignedTo.All )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.createParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun updateParticipantGroup_succeeds_remove_old_and_add_new() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val newParticipant = recruitmentService.addParticipant( studyId, EmailAddress( "test2@test.com" ) )

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val newAssignedParticipant = AssignedParticipantRoles( newParticipant.id, AssignedTo.All )
        val stagedParticipantGroup = recruitmentService.createParticipantGroup( studyId, setOf( assignParticipant ) )
        val updatedGroupStatus = recruitmentService.updateParticipantGroup( studyId, stagedParticipantGroup.id, setOf( newAssignedParticipant ) )
        assertEquals( newParticipant, updatedGroupStatus.participants.single() )
        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        val participantInGroup = participantGroups.single().participants.single()
        assertEquals( newParticipant, participantInGroup )
    }

    @Test
    fun updateParticipantGroup_succeeds_keep_old_and_add_new() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val newParticipant = recruitmentService.addParticipant( studyId, EmailAddress( "test2@test.com" ) )

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val newAssignedParticipant = AssignedParticipantRoles( newParticipant.id, AssignedTo.All )
        val stagedParticipantGroup = recruitmentService.createParticipantGroup( studyId, setOf( assignParticipant ) )
        val updatedGroupStatus = recruitmentService.updateParticipantGroup( studyId, stagedParticipantGroup.id, setOf( assignParticipant, newAssignedParticipant ) )
        assertEquals<Set<Participant>>( setOf( participant, newParticipant ), updatedGroupStatus.participants )
        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        val participantInGroup = participantGroups.single().participants
        assertEquals( 2, participantInGroup.size )
    }

    @Test
    fun updateParticipantGroup_succeeds_overlapping_participants() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val oldParticipants = setOf( "test0@test.com", "test1@test.com" )
            .map { recruitmentService.addParticipant( studyId, EmailAddress( it ) ) }.toSet()
        val oldAssignedParticipants = oldParticipants
            .map { AssignedParticipantRoles( it.id, AssignedTo.All ) }.toSet()
        val newParticipants = setOf( "test2@test.com", "test3@test.com" )
            .map { recruitmentService.addParticipant( studyId, EmailAddress( it ) ) }.toSet()
        val newAssignedParticipants = newParticipants
            .map { AssignedParticipantRoles( it.id, AssignedTo.All ) }.toSet()

        val stagedParticipantGroup = recruitmentService.createParticipantGroup( studyId, oldAssignedParticipants)
        val updatedGroupStatus = recruitmentService.updateParticipantGroup( studyId, stagedParticipantGroup.id, newAssignedParticipants )
        assertEquals( newParticipants, updatedGroupStatus.participants )
        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        val participantInGroup = participantGroups.single().participants
        assertEquals( 2, participantInGroup.size )
    }

    @Test
    fun updateParticipantGroup_fails_for_already_deployed_group() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.createParticipantGroup( studyId, setOf( assignParticipant ) )
        val deployedGroup = recruitmentService.inviteParticipantGroup( studyId, groupStatus.id )
        assertFailsWith<IllegalStateException>
        {
            recruitmentService.updateParticipantGroup( studyId, deployedGroup.id, setOf( assignParticipant ) )
        }
    }

    @Test
    fun updateParticipantGroup_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )

        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.updateParticipantGroup( unknownId, unknownId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun updateParticipantGroup_fails_for_unknown_participantGroupId() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val assignParticipant = AssignedParticipantRoles( UUID.randomUUID(), AssignedTo.All )

        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.updateParticipantGroup( studyId, unknownId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun updateParticipantGroup_fails_for_unknown_participants() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        val assignParticipant = AssignedParticipantRoles( unknownId, AssignedTo.All )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.createParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun inviteParticipantGroup_fails_for_unknown_participant_roles() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.Roles( setOf( "Unknown role" ) ) )

        val stagedParticipantGroup = recruitmentService.createParticipantGroup( studyId, setOf( assignParticipant ) )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.inviteParticipantGroup( studyId, stagedParticipantGroup.id )
        }
    }

    @Test
    fun inviteParticipantGroup_fails_when_not_all_participant_roles_assigned() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, protocol) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val role = protocol.participantRoles.first().role
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.Roles( setOf( role ) ) )
        val stagedParticipantGroup = recruitmentService.createParticipantGroup( studyId, setOf( assignParticipant ) )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.inviteParticipantGroup( studyId, stagedParticipantGroup.id )
        }
    }

    @Test
    fun inviteParticipantGroup_multiple_times_returns_same_group() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val stagedParticipantGroup = recruitmentService.createParticipantGroup( studyId, setOf( assignParticipant ) )
        val groupStatus = recruitmentService.inviteParticipantGroup( studyId, stagedParticipantGroup.id )

        // Deploy the same group a second time.
        val groupStatus2 = recruitmentService.inviteParticipantGroup( studyId, stagedParticipantGroup.id )
        assertEquals( groupStatus, groupStatus2 )
    }

    @Test
    fun getParticipantGroupStatusList_returns_multiple_deployments() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        val p1 = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignedP1 = AssignedParticipantRoles( p1.id, AssignedTo.All )
        recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignedP1 ) )

        val p2 = recruitmentService.addParticipant( studyId, EmailAddress( "test2@test.com" ) )
        val assignedP2 = AssignedParticipantRoles( p2.id, AssignedTo.All )
        recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignedP2 ) )

        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        assertEquals( 2, participantGroups.size )
        val deployedParticipants = participantGroups.flatMap { it.participants }.toSet()
        assertEquals( setOf( p1, p2 ), deployedParticipants )
    }

    @Test
    fun getParticipantGroupStatusLists_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()

        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipantGroupStatusList( unknownId ) }
    }

    @Test
    fun stopParticipantGroup_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )

        val stoppedGroupStatus = recruitmentService.stopParticipantGroup( studyId, groupStatus.id )
        assertTrue( stoppedGroupStatus is ParticipantGroupStatus.Stopped )
    }

    @Test
    fun stopParticipantGroup_fails_with_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()

        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.stopParticipantGroup( unknownId, UUID.randomUUID() )
        }
    }

    @Test
    fun stopParticipantGroup_fails_with_unknown_groupId() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        assertFailsWith<IllegalArgumentException> { recruitmentService.stopParticipantGroup( studyId, unknownId ) }
    }


    private suspend fun createLiveStudy( service: StudyService ): Pair<UUID, StudyProtocolSnapshot>
    {
        // Create deployable protocol.
        val protocol = StudyProtocol( UUID.randomUUID(), "Test protocol" )
        protocol.addPrimaryDevice( Smartphone( "User's phone" ) )
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        )
        protocol.addParticipantRole( ParticipantRole( "Test role", false ) )
        protocol.addParticipantRole( ParticipantRole( "Test role 2", false ) )
        protocol.addExpectedParticipantData( expectedData )
        val validSnapshot = protocol.getSnapshot()

        // Create live study from protocol.
        val status = service.createStudy( UUID.randomUUID(), "Test" )
        val studyId = status.studyId
        service.setProtocol( studyId, validSnapshot )
        service.goLive( studyId )

        return Pair( studyId, validSnapshot )
    }
}
