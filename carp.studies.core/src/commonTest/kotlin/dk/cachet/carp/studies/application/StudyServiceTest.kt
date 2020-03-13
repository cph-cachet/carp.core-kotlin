package dk.cachet.carp.studies.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.studies.domain.ConfiguringStudyStatus
import dk.cachet.carp.studies.domain.LiveStudyStatus
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for implementations of [StudyService].
 */
interface StudyServiceTest
{
    /**
     * Create a user service and repository it depends on to be used in the tests.
     */
    fun createService(): Pair<StudyService, StudyRepository>


    @Test
    fun createStudy_succeeds() = runBlockingTest {
        val ( service, repo ) = createService()

        val owner = StudyOwner()
        val name = "Test"
        val status = service.createStudy( owner, name )

        // Verify whether study was added to the repository.
        val foundStudy = repo.getById( status.studyId )
        assertNotNull( foundStudy )
        assertEquals( status.studyId, foundStudy.id )
        assertEquals( name, foundStudy.name )
        assertEquals( name, foundStudy.invitation.name ) // Default study description when not specified.
        assertFalse( foundStudy.canDeployToParticipants )
    }

    @Test
    fun createStudy_with_invitation_succeeds() = runBlockingTest {
        val ( service, repo ) = createService()

        val owner = StudyOwner()
        val name = "Test"
        val invitation = StudyInvitation( "Lorem ipsum" )
        val status = service.createStudy( owner, name, invitation )

        val foundStudy = repo.getById( status.studyId )!!
        assertEquals( status.studyId, foundStudy.id )
        assertEquals( name, foundStudy.name )
        assertEquals( invitation, foundStudy.invitation )
        assertFalse( foundStudy.canDeployToParticipants )
    }

    @Test
    fun getStudyStatus_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        val foundStatus = service.getStudyStatus( status.studyId )
        assertEquals( status, foundStatus )
    }

    @Test
    fun getStudyStatus_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        assertFailsWith<IllegalArgumentException> { service.getStudyStatus( UUID.randomUUID() ) }
    }

    @Test
    fun getStudiesOverview_returns_owner_studies() = runBlockingTest {
        val ( service, _ ) = createService()
        val owner = StudyOwner()
        val studyOne = service.createStudy( owner, "One" )
        val studyTwo = service.createStudy( owner, "Two" )
        service.createStudy( StudyOwner(), "Three" )

        val studiesOverview = service.getStudiesOverview( owner )
        val expectedStudies = listOf( studyOne, studyTwo )
        assertEquals( 2, studiesOverview.intersect( expectedStudies ).count() )
    }

    @Test
    fun adding_and_retrieving_participant_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val owner = StudyOwner()
        val study = service.createStudy( owner, "Test" )
        val studyId = study.studyId

        val participant = service.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val studyParticipants = service.getParticipants( studyId )
        assertEquals( participant, studyParticipants.single() )
    }

    @Test
    fun addParticipant_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        val unknownId = UUID.randomUUID()
        val email = EmailAddress( "test@test.com" )
        assertFailsWith<IllegalArgumentException> { service.addParticipant( unknownId, email ) }
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipant_twice_returns_same_participant() = runBlockingTest {
        val ( service, _ ) = createService()
        val study = service.createStudy( StudyOwner(), "Test" )
        val studyId = study.studyId

        val email = EmailAddress( "test@test.com" )
        val p1 = service.addParticipant( studyId, email )
        val p2 = service.addParticipant( studyId, email )
        assertTrue( p1 == p2 )
    }

    @Test
    fun getParticipants_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { service.getParticipants( unknownId ) }
    }

    @Test
    fun setProtocol_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        var status = service.createStudy( StudyOwner(), "Test" )

        status = service.setProtocol( status.studyId, createDeployableProtocol() )
        assertFalse( status.canDeployToParticipants )
        assertTrue( status is ConfiguringStudyStatus )
    }

    @Test
    fun setProtocol_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()
        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { service.setProtocol( unknownId, createDeployableProtocol() ) }
    }

    @Test
    fun setProtocol_fails_for_invalid_protocol_snapshot() = runBlockingTest {
        val ( service, _ ) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        val validSnapshot = createDeployableProtocol()
        val invalidSnapshot = validSnapshot.copy(
            triggeredTasks = listOf(
                StudyProtocolSnapshot.TriggeredTask( 0, "Unknown task", "Unknown device" )
            )
        )
        assertFailsWith<IllegalArgumentException> { service.setProtocol( status.studyId, invalidSnapshot ) }
    }

    @Test
    fun setProtocol_fails_for_protocol_which_cant_be_deployed() = runBlockingTest {
        val ( service, _ ) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        val protocol = StudyProtocol( ProtocolOwner(), "Not deployable" )
        assertFailsWith<IllegalArgumentException> { service.setProtocol( status.studyId, protocol.getSnapshot() ) }
    }

    @Test
    fun goLive_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()

        var status = service.createStudy( StudyOwner(), "Test" )
        assertTrue( status is ConfiguringStudyStatus )

        // Set protocol and go live.
        service.setProtocol( status.studyId, createDeployableProtocol() )
        status = service.goLive( status.studyId )
        assertTrue( status.canDeployToParticipants )
        assertTrue( status is LiveStudyStatus )
    }

    @Test
    fun goLive_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()
        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { service.goLive( unknownId ) }
    }

    @Test
    fun goLive_fails_when_no_protocol_set_yet() = runBlockingTest {
        val ( service, _ ) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        assertFailsWith<IllegalStateException> { service.goLive( status.studyId ) }
    }

    @Test
    fun deployParticipantGroup_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val ( studyId, protocolSnapshot ) = createLiveStudy( service )
        val participant = service.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        val assignParticipant = AssignParticipantDevices( participant.id, deviceRoles )
        service.deployParticipantGroup( studyId, setOf( assignParticipant ) )
    }

    @Test
    fun deployParticipantGroup_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()
        val unknownId = UUID.randomUUID()
        val assignParticipant = AssignParticipantDevices( UUID.randomUUID(), setOf( "Test device" ) )
        assertFailsWith<IllegalArgumentException> { service.deployParticipantGroup( unknownId, setOf( assignParticipant ) ) }
    }

    @Test
    fun deployParticipantGroup_fails_for_empty_group() = runBlockingTest {
        val ( service, _ ) = createService()
        val ( studyId, _ ) = createLiveStudy( service )

        assertFailsWith<IllegalArgumentException> { service.deployParticipantGroup( studyId, setOf() ) }
    }

    @Test
    fun deployParticipantGroup_fails_for_unknown_participants() = runBlockingTest {
        val ( service, _ ) = createService()
        val ( studyId, protocolSnapshot ) = createLiveStudy( service )

        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        val unknownParticipantId = UUID.randomUUID()
        val assignParticipant = AssignParticipantDevices( unknownParticipantId, deviceRoles )
        assertFailsWith<IllegalArgumentException> { service.deployParticipantGroup( studyId, setOf( assignParticipant ) ) }
    }

    @Test
    fun deployParticipantGroup_fails_for_unknown_device_roles() = runBlockingTest {
        val ( service, _ ) = createService()
        val ( studyId, _ ) = createLiveStudy( service )
        val participant = service.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignParticipantDevices( participant.id, setOf( "Unknown device" ) )
        assertFailsWith<IllegalArgumentException> { service.deployParticipantGroup( studyId, setOf( assignParticipant ) ) }
    }

    @Test
    fun deployParticipantGroup_fails_when_not_all_devices_assigned() = runBlockingTest {
        val ( service, _ ) = createService()
        val ( studyId, _ ) = createLiveStudy( service )
        val participant = service.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignParticipantDevices( participant.id, setOf() )
        assertFailsWith<IllegalArgumentException> { service.deployParticipantGroup( studyId, setOf( assignParticipant ) ) }
    }


    private fun createDeployableProtocol(): StudyProtocolSnapshot
    {
        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )

        // Add master device, needed for it to be deployable.
        protocol.addMasterDevice( Smartphone( "User's phone" ) )

        return protocol.getSnapshot()
    }

    private suspend fun createLiveStudy( service: StudyService ): Pair<UUID, StudyProtocolSnapshot>
    {
        val status = service.createStudy( StudyOwner(), "Test" )
        val studyId = status.studyId
        val validSnapshot = createDeployableProtocol()
        service.setProtocol( studyId, validSnapshot )
        service.goLive( studyId )

        return Pair( studyId, validSnapshot )
    }
}
