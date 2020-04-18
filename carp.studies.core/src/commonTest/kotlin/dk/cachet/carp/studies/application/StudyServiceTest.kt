package dk.cachet.carp.studies.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


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

        // Default study description when not specified.
        assertEquals( name, foundStudy.invitation.name )
        assertEquals( "", foundStudy.invitation.description )

        assertFalse( foundStudy.canDeployToParticipants )
    }

    @Test
    fun createStudy_with_invitation_succeeds() = runBlockingTest {
        val ( service, repo ) = createService()

        val owner = StudyOwner()
        val name = "Test"
        val description = "Description"
        val invitation = StudyInvitation( "Lorem ipsum", "Some description" )
        val status = service.createStudy( owner, name, description, invitation )

        val foundStudy = repo.getById( status.studyId )!!
        assertEquals( status.studyId, foundStudy.id )
        assertEquals( name, foundStudy.name )
        assertEquals( description, foundStudy.description )
        assertEquals( invitation, foundStudy.invitation )
        assertFalse( foundStudy.canDeployToParticipants )
    }

    @Test
    fun setInternalDescription_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        val newName = "New name"
        val newDescription = "New description"
        val updatedStatus = service.setInternalDescription( status.studyId, newName, newDescription )
        assertEquals( newName, updatedStatus.name )
        val studyDetails = service.getStudyDetails( status.studyId )
        assertEquals( newName, studyDetails.name )
        assertEquals( newDescription, studyDetails.description )
    }

    @Test
    fun setInternalDescription_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        assertFailsWith<IllegalArgumentException> { service.setInternalDescription( UUID.randomUUID(), "New name", "New description" ) }
    }

    @Test
    fun getStudyDetails_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        assertFailsWith<IllegalArgumentException> { service.getStudyDetails( unknownId ) }
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

        assertFailsWith<IllegalArgumentException> { service.getStudyStatus( unknownId ) }
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

        assertFailsWith<IllegalArgumentException> { service.getParticipants( unknownId ) }
    }

    @Test
    fun setInvitation_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        assertTrue( status.canSetInvitation )
        val invitation = StudyInvitation( "Study name", "Description" )
        service.setInvitation( status.studyId, invitation )
        val studyDetails = service.getStudyDetails( status.studyId )
        assertEquals( invitation, studyDetails.invitation )
    }

    @Test
    fun setInvitation_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        assertFailsWith<IllegalArgumentException> { service.setInvitation( unknownId, StudyInvitation.empty() ) }
    }

    @Test
    fun setProtocol_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        var status = service.createStudy( StudyOwner(), "Test" )

        assertTrue( status.canSetStudyProtocol )
        val protocol = createDeployableProtocol()
        status = service.setProtocol( status.studyId, protocol )
        assertFalse( status.canDeployToParticipants )
        assertTrue( status is StudyStatus.Configuring )

        val details = service.getStudyDetails( status.studyId )
        assertEquals( protocol, details.protocolSnapshot )
    }

    @Test
    fun setProtocol_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

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
    fun setInvitation_and_setProtocol_fails_after_study_gone_live() = runBlockingTest {
        val ( service, _ ) = createService()
        var status = service.createStudy( StudyOwner(), "Test" )

        val protocol = createDeployableProtocol()
        service.setProtocol( status.studyId, protocol )
        status = service.goLive( status.studyId )
        assertFalse( status.canSetInvitation )
        assertFalse( status.canSetStudyProtocol )

        assertFailsWith<IllegalStateException> { service.setInvitation( status.studyId, StudyInvitation.empty() ) }
        assertFailsWith<IllegalStateException> { service.setProtocol( status.studyId, protocol ) }
    }

    @Test
    fun goLive_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()

        var status = service.createStudy( StudyOwner(), "Test" )
        assertTrue( status is StudyStatus.Configuring )

        // Set protocol and go live.
        service.setProtocol( status.studyId, createDeployableProtocol() )
        status = service.goLive( status.studyId )
        assertTrue( status.canDeployToParticipants )
        assertTrue( status is StudyStatus.Live )
    }

    @Test
    fun goLive_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

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
        val groupStatus = service.deployParticipantGroup( studyId, setOf( assignParticipant ) )
        assertEquals( participant.id, groupStatus.participants.single().participantId )
        val participantGroups = service.getParticipantGroupStatuses( studyId )
        val participantIdInGroup = participantGroups.single().participants.single().participantId
        assertEquals( participant.id, participantIdInGroup )
    }

    @Test
    fun deployParticipantGroup_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()
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
        val assignParticipant = AssignParticipantDevices( unknownId, deviceRoles )
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

    @Test
    fun getParticipantGroupStatuses_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        assertFailsWith<IllegalArgumentException> { service.getParticipantGroupStatuses( unknownId ) }
    }

    @Test
    fun stopParticipantGroup_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val ( studyId, protocolSnapshot ) = createLiveStudy( service )
        val participant = service.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val deviceRoles = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        val assignParticipant = AssignParticipantDevices( participant.id, deviceRoles )
        val groupStatus = service.deployParticipantGroup( studyId, setOf( assignParticipant ) )

        val stoppedGroupStatus = service.stopParticipantGroup( studyId, groupStatus.id )
        assertTrue( stoppedGroupStatus.studyDeploymentStatus is StudyDeploymentStatus.Stopped )
    }

    @Test
    fun stopParticipantGroup_fails_with_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        assertFailsWith<IllegalArgumentException> { service.stopParticipantGroup( unknownId, UUID.randomUUID() ) }
    }

    @Test
    fun stopParticipantGroup_fails_with_unknown_groupId() = runBlockingTest {
        val ( service, _ ) = createService()
        val ( studyId, _ ) = createLiveStudy( service )

        assertFailsWith<IllegalArgumentException> { service.stopParticipantGroup( studyId, unknownId ) }
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
