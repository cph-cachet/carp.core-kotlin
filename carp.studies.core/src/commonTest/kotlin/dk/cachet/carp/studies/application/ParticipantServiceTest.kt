package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.StudyProtocolSnapshot
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.domain.ProtocolOwner
import dk.cachet.carp.common.domain.StudyProtocol
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.test.runSuspendTest
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
    fun adding_and_retrieving_participant_succeeds() = runSuspendTest {
        val (participantService, studyService) = createService()
        val study = studyService.createStudy( StudyOwner(), "Test" )
        val studyId = study.studyId

        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        // Get single participant.
        val studyParticipant = participantService.getParticipant( studyId, participant.id )
        assertEquals( participant, studyParticipant )

        // Get all participants.
        val studyParticipants = participantService.getParticipants( studyId )
        assertEquals( participant, studyParticipants.single() )
    }

    @Test
    fun addParticipant_fails_for_unknown_studyId() = runSuspendTest {
        val (participantService, _) = createService()

        val email = EmailAddress( "test@test.com" )
        assertFailsWith<IllegalArgumentException> { participantService.addParticipant( unknownId, email ) }
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipant_twice_returns_same_participant() = runSuspendTest {
        val (participantService, studyService) = createService()
        val study = studyService.createStudy( StudyOwner(), "Test" )
        val studyId = study.studyId

        val email = EmailAddress( "test@test.com" )
        val p1 = participantService.addParticipant( studyId, email )
        val p2 = participantService.addParticipant( studyId, email )
        assertTrue( p1 == p2 )
    }

    @Test
    fun getParticipant_fails_for_unknown_id() = runSuspendTest {
        val (participantService, studyService) = createService()
        val study = studyService.createStudy( StudyOwner(), "Test" )

        // Unknown study Id.
        assertFailsWith<IllegalArgumentException> { participantService.getParticipant( unknownId, unknownId ) }

        // Unknown participant Id.
        assertFailsWith<IllegalArgumentException> { participantService.getParticipant( study.studyId, unknownId ) }
    }

    @Test
    fun getParticipants_fails_for_unknown_studyId() = runSuspendTest {
        val (participantService, _) = createService()

        assertFailsWith<IllegalArgumentException> { participantService.getParticipants( unknownId ) }
    }

    @Test
    fun deployParticipantGroup_succeeds() = runSuspendTest {
        val (participantService, studyService) = createService()
        val (studyId, protocolSnapshot) = createLiveStudy( studyService )
        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        val assignParticipant = AssignParticipantDevices( participant.id, deviceRoles )
        val groupStatus = participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )
        assertEquals( participant.id, groupStatus.participants.single().participantId )
        assertNull( groupStatus.data[ CarpInputDataTypes.SEX ] ) // By default, the configured expected data is not set.
        val participantGroups = participantService.getParticipantGroupStatusList( studyId )
        val participantIdInGroup = participantGroups.single().participants.single().participantId
        assertEquals( participant.id, participantIdInGroup )
    }

    @Test
    fun deployParticipantGroup_fails_for_unknown_studyId() = runSuspendTest {
        val (participantService, _) = createService()
        val assignParticipant = AssignParticipantDevices( UUID.randomUUID(), setOf( "Test device" ) )

        assertFailsWith<IllegalArgumentException>
        {
            participantService.deployParticipantGroup( unknownId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun deployParticipantGroup_fails_for_empty_group() = runSuspendTest {
        val (participantService, studyService) = createService()
        val (studyId, _) = createLiveStudy( studyService )

        assertFailsWith<IllegalArgumentException> { participantService.deployParticipantGroup( studyId, setOf() ) }
    }

    @Test
    fun deployParticipantGroup_fails_for_unknown_participants() = runSuspendTest {
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
    fun deployParticipantGroup_fails_for_unknown_device_roles() = runSuspendTest {
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
    fun deployParticipantGroup_fails_when_not_all_devices_assigned() = runSuspendTest {
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
    fun deployParticipantGroup_multiple_times_returns_same_group() = runSuspendTest {
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
    fun deployParticipantGroup_for_previously_stopped_group_returns_new_group() = runSuspendTest {
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
    fun getParticipantGroupStatusList_returns_multiple_deployments() = runSuspendTest {
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
    fun getParticipantGroupStatusLists_fails_for_unknown_studyId() = runSuspendTest {
        val (participantService, _) = createService()

        assertFailsWith<IllegalArgumentException> { participantService.getParticipantGroupStatusList( unknownId ) }
    }

    @Test
    fun stopParticipantGroup_succeeds() = runSuspendTest {
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
    fun stopParticipantGroup_fails_with_unknown_studyId() = runSuspendTest {
        val (participantService, _) = createService()

        assertFailsWith<IllegalArgumentException>
        {
            participantService.stopParticipantGroup( unknownId, UUID.randomUUID() )
        }
    }

    @Test
    fun stopParticipantGroup_fails_with_unknown_groupId() = runSuspendTest {
        val (participantService, studyService) = createService()
        val (studyId, _) = createLiveStudy( studyService )

        assertFailsWith<IllegalArgumentException> { participantService.stopParticipantGroup( studyId, unknownId ) }
    }

    @Test
    fun setParticipantGroupData_succeeds() = runSuspendTest {
        val (participantService, studyService) = createService()
        val (studyId, snapshot) = createLiveStudy( studyService )
        val group = createLiveGroup( participantService, studyId, snapshot )

        val expectedData = CarpInputDataTypes.SEX
        val groupAfterSet = participantService.setParticipantGroupData( studyId, group.id, expectedData, Sex.Male )
        assertEquals( 1, groupAfterSet.data.size )
        assertEquals( Sex.Male, groupAfterSet.data[ expectedData ] )

        val retrievedGroups = participantService.getParticipantGroupStatusList( studyId )
        val retrievedGroup = retrievedGroups.firstOrNull { it.id == group.id }
        assertNotNull( retrievedGroup )
        assertEquals( groupAfterSet.data, retrievedGroup.data )
    }

    @Test
    fun setParticipantGroupData_fails_with_unknown_id() = runSuspendTest {
        val (participantService, _) = createService()

        assertFailsWith<IllegalArgumentException>
        {
            participantService.setParticipantGroupData( unknownId, unknownId, CarpInputDataTypes.SEX, Sex.Male )
        }
    }

    @Test
    fun setParticipantGroupData_fails_for_unexpected_data() = runSuspendTest {
        val (participantService, studyService) = createService()
        val (studyId, snapshot) = createLiveStudy( studyService )
        val group = createLiveGroup( participantService, studyId, snapshot )

        val unexpectedData = InputDataType( "namespace", "type" )
        assertFailsWith<IllegalArgumentException> {
            participantService.setParticipantGroupData( studyId, group.id, unexpectedData, null )
        }
    }


    private suspend fun createLiveStudy( service: StudyService ): Pair<UUID, StudyProtocolSnapshot>
    {
        // Create deployable protocol.
        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
        protocol.addMasterDevice( Smartphone( "User's phone" ) )
        protocol.addExpectedParticipantData( ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ) )
        val validSnapshot = protocol.getSnapshot()

        // Create live study from protocol.
        val status = service.createStudy( StudyOwner(), "Test" )
        val studyId = status.studyId
        service.setProtocol( studyId, validSnapshot )
        service.goLive( studyId )

        return Pair( studyId, validSnapshot )
    }

    private suspend fun createLiveGroup(
        participantService: ParticipantService,
        studyId: UUID,
        protocolSnapshot: StudyProtocolSnapshot
    ): ParticipantGroupStatus
    {
        val participant = participantService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        val assignParticipant = AssignParticipantDevices( participant.id, deviceRoles )

        return participantService.deployParticipantGroup( studyId, setOf( assignParticipant ) )
    }
}
