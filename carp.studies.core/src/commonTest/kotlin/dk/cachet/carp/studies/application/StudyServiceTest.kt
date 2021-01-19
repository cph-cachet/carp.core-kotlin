package dk.cachet.carp.studies.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ApplicationServiceEventBus
import dk.cachet.carp.common.ddd.subscribe
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [StudyService].
 */
interface StudyServiceTest
{
    /**
     * Create a study service to be used in the tests.
     */
    fun createService(): Pair<StudyService, ApplicationServiceEventBus<StudyService, StudyService.Event>>


    @Test
    fun createStudy_succeeds() = runSuspendTest {
        val (service, _) = createService()

        val owner = StudyOwner()
        val name = "Test"
        val status = service.createStudy( owner, name )

        // Verify whether study was added.
        val foundStudy = service.getStudyStatus( status.studyId )
        assertEquals( status.studyId, foundStudy.studyId )
        assertEquals( name, foundStudy.name )

        // Default study description when not specified.
        val studyDetails = service.getStudyDetails( status.studyId )
        assertEquals( name, studyDetails.invitation.name )
        assertEquals( "", studyDetails.invitation.description )

        assertFalse( foundStudy.canDeployToParticipants )
    }

    @Test
    fun createStudy_with_invitation_succeeds() = runSuspendTest {
        val (service, _) = createService()

        val owner = StudyOwner()
        val name = "Test"
        val description = "Description"
        val invitation = StudyInvitation( "Lorem ipsum", "Some description" )
        val status = service.createStudy( owner, name, description, invitation )

        val foundStudy = service.getStudyStatus( status.studyId )
        assertEquals( status.studyId, foundStudy.studyId )
        assertEquals( name, foundStudy.name )
        val studyDetails = service.getStudyDetails( status.studyId )
        assertEquals( description, studyDetails.description )
        assertEquals( invitation, studyDetails.invitation )
        assertFalse( foundStudy.canDeployToParticipants )
    }

    @Test
    fun setInternalDescription_succeeds() = runSuspendTest {
        val (service, _) = createService()
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
    fun setInternalDescription_fails_for_unknown_studyId() = runSuspendTest {
        val (service, _) = createService()

        assertFailsWith<IllegalArgumentException>
        {
            service.setInternalDescription( UUID.randomUUID(), "New name", "New description" )
        }
    }

    @Test
    fun getStudyDetails_fails_for_unknown_studyId() = runSuspendTest {
        val (service, _) = createService()

        assertFailsWith<IllegalArgumentException> { service.getStudyDetails( unknownId ) }
    }

    @Test
    fun getStudyStatus_succeeds() = runSuspendTest {
        val (service, _) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        val foundStatus = service.getStudyStatus( status.studyId )
        assertEquals( status, foundStatus )
    }

    @Test
    fun getStudyStatus_fails_for_unknown_studyId() = runSuspendTest {
        val (service, _) = createService()

        assertFailsWith<IllegalArgumentException> { service.getStudyStatus( unknownId ) }
    }

    @Test
    fun getStudiesOverview_returns_owner_studies() = runSuspendTest {
        val (service, _) = createService()

        val owner = StudyOwner()
        val studyOne = service.createStudy( owner, "One" )
        val studyTwo = service.createStudy( owner, "Two" )
        service.createStudy( StudyOwner(), "Three" )

        val studiesOverview = service.getStudiesOverview( owner )
        val expectedStudies = listOf( studyOne, studyTwo )
        assertEquals( 2, studiesOverview.intersect( expectedStudies ).count() )
    }

    @Test
    fun setInvitation_succeeds() = runSuspendTest {
        val (service, _) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        assertTrue( status.canSetInvitation )
        val invitation = StudyInvitation( "Study name", "Description" )
        service.setInvitation( status.studyId, invitation )
        val studyDetails = service.getStudyDetails( status.studyId )
        assertEquals( invitation, studyDetails.invitation )
    }

    @Test
    fun setInvitation_fails_for_unknown_studyId() = runSuspendTest {
        val (service, _) = createService()

        assertFailsWith<IllegalArgumentException> { service.setInvitation( unknownId, StudyInvitation.empty() ) }
    }

    @Test
    fun setProtocol_succeeds() = runSuspendTest {
        val (service, _) = createService()
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
    fun setProtocol_fails_for_unknown_studyId() = runSuspendTest {
        val (service, _) = createService()

        assertFailsWith<IllegalArgumentException> { service.setProtocol( unknownId, createDeployableProtocol() ) }
    }

    @Test
    fun setProtocol_fails_for_invalid_protocol_snapshot() = runSuspendTest {
        val (service, _) = createService()
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
    fun setProtocol_fails_for_protocol_which_cant_be_deployed() = runSuspendTest {
        val (service, _) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        val protocol = StudyProtocol( ProtocolOwner(), "Not deployable" )
        assertFailsWith<IllegalArgumentException> { service.setProtocol( status.studyId, protocol.getSnapshot() ) }
    }

    @Test
    fun setInvitation_and_setProtocol_fails_after_study_gone_live() = runSuspendTest {
        val (service, _) = createService()
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
    fun goLive_succeeds() = runSuspendTest {
        val (service, _) = createService()

        var status = service.createStudy( StudyOwner(), "Test" )
        assertTrue( status is StudyStatus.Configuring )

        // Set protocol and go live.
        service.setProtocol( status.studyId, createDeployableProtocol() )
        status = service.goLive( status.studyId )
        assertTrue( status.canDeployToParticipants )
        assertTrue( status is StudyStatus.Live )
    }

    @Test
    fun goLive_fails_for_unknown_studyId() = runSuspendTest {
        val (service, _) = createService()

        assertFailsWith<IllegalArgumentException> { service.goLive( unknownId ) }
    }

    @Test
    fun goLive_fails_when_no_protocol_set_yet() = runSuspendTest {
        val (service, _) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        assertFailsWith<IllegalStateException> { service.goLive( status.studyId ) }
    }

    @Test
    fun remove_succeeds() = runSuspendTest {
        val (service, eventBus) = createService()
        val owner = StudyOwner()
        val status = service.createStudy( owner, "Test" )

        var removedEvent: StudyService.Event.StudyRemoved? = null
        eventBus.subscribe { removed: StudyService.Event.StudyRemoved -> removedEvent = removed }
        val isRemoved = service.remove( status.studyId )

        assertTrue( isRemoved )
        val studies = service.getStudiesOverview( owner )
        assertTrue( studies.isEmpty() )
        assertEquals( status.studyId, removedEvent?.studyId )
    }

    @Test
    fun remove_returns_false_when_already_removed() = runSuspendTest {
        val (service, eventBus) = createService()
        val owner = StudyOwner()
        val status = service.createStudy( owner, "Test" )
        service.remove( status.studyId )

        var removedEvent: StudyService.Event.StudyRemoved? = null
        eventBus.subscribe { removed: StudyService.Event.StudyRemoved -> removedEvent = removed }
        val isRemoved = service.remove( status.studyId )

        assertFalse( isRemoved )
        assertNull( removedEvent )
    }


    private fun createDeployableProtocol(): StudyProtocolSnapshot
    {
        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )

        // Add master device, needed for it to be deployable.
        protocol.addMasterDevice( Smartphone( "User's phone" ) )

        return protocol.getSnapshot()
    }
}
