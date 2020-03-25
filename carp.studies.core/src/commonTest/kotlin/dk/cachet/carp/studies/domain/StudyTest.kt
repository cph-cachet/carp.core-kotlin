package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.StudyOwner
import kotlin.test.*


/**
 * Tests for [Study].
 */
class StudyTest
{
    @Test
    fun creating_study_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val study = createComplexStudy()

        val snapshot = study.getSnapshot()
        val fromSnapshot = Study.fromSnapshot( snapshot )

        assertEquals( study.id, fromSnapshot.id )
        assertEquals( study.owner, fromSnapshot.owner )
        assertEquals( study.name, fromSnapshot.name )
        assertEquals( study.description, fromSnapshot.description )
        assertEquals( study.invitation, fromSnapshot.invitation )
        assertEquals( study.creationDate, fromSnapshot.creationDate )
        assertEquals( study.protocolSnapshot, fromSnapshot.protocolSnapshot )
        assertEquals( study.isLive, fromSnapshot.isLive )
        assertEquals( study.participations, fromSnapshot.participations )
    }

    @Test
    fun set_invitation_succeeds()
    {
        val study = createStudy()

        assertTrue( study.canSetInvitation )

        val invitation = StudyInvitation( "Test study" )
        study.invitation = invitation
        assertEquals( invitation, study.invitation )
    }

    @Test
    fun set_invitation_fails_for_live_study()
    {
        val study = createStudy()
        setDeployableProtocol( study )
        study.goLive()

        assertFailsWith<IllegalStateException> { study.invitation = StudyInvitation.empty() }
    }

    @Test
    fun set_protocol_succeeds()
    {
        val study = createStudy()

        assertTrue( study.canSetStudyProtocol )

        setDeployableProtocol( study )
        assertEquals( Study.Event.ProtocolSnapshotChanged( study.protocolSnapshot ), study.consumeEvents().single() )
    }

    @Test
    fun set_protocol_to_null_succeeds()
    {
        val study = createStudy()
        study.protocolSnapshot = null
        assertEquals( Study.Event.ProtocolSnapshotChanged( null ), study.consumeEvents().single() )
    }

    @Test
    fun set_protocol_fails_for_protocol_with_deployment_errors()
    {
        val study = createStudy()

        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
        assertFailsWith<IllegalArgumentException> { study.protocolSnapshot = protocol.getSnapshot() }
        assertEquals( 0, study.consumeEvents().count() )
    }

    @Test
    fun verify_state_of_new_study()
    {
        val study = createStudy()
        assertFalse( study.canDeployToParticipants )
        assertFalse( study.isLive )

        val status = study.getStatus()
        assertEquals( study.canSetInvitation, status.canSetInvitation )
        assertEquals( study.canSetStudyProtocol, status.canSetStudyProtocol )
        assertEquals( study.canDeployToParticipants, status.canDeployToParticipants )
        assertTrue( status is StudyStatus.Configuring )
        assertFalse( status.canGoLive )
    }

    @Test
    fun canDeployToParticipants_false_until_live()
    {
        val study = createStudy()
        assertFalse( study.canDeployToParticipants )

        // Define protocol.
        setDeployableProtocol( study )
        assertFalse( study.canDeployToParticipants )

        // Go live.
        study.goLive()
        assertTrue( study.canDeployToParticipants )
        assertEquals( Study.Event.StateChanged( true ), study.consumeEvents().last() )
    }

    @Test
    fun canSetInvitation_and_canSetStudyProtocol_true_until_live()
    {
        val study = createStudy()
        assertTrue( study.canSetInvitation )
        assertTrue( study.canSetStudyProtocol )

        // Go live.
        setDeployableProtocol( study )
        study.goLive()
        assertFalse( study.canSetInvitation )
        assertFalse( study.canSetStudyProtocol )
    }

    @Test
    fun goLive_can_be_called_more_than_once()
    {
        val study = createStudy()
        setDeployableProtocol( study )
        study.goLive()

        // Study already live, but should not fail.
        study.goLive()
        val stateEvents = study.consumeEvents().filterIsInstance<Study.Event.StateChanged>()
        assertEquals( 1, stateEvents.count() )
    }

    @Test
    fun goLive_fails_when_no_protocol_set()
    {
        val study = createStudy()
        val status = study.getStatus()

        assertTrue( status is StudyStatus.Configuring )
        assertFalse( status.canGoLive )

        assertFailsWith<IllegalStateException> { study.goLive() }
        assertEquals( 0, study.consumeEvents().count() )
    }

    @Test
    fun addParticipation_succeeds()
    {
        val study = createStudy()
        setDeployableProtocol( study )
        study.goLive()

        assertTrue( study.canDeployToParticipants )

        val participation = DeanonymizedParticipation( UUID.randomUUID(), Participation( UUID.randomUUID() ) )
        study.addParticipation( participation )
        assertEquals( Study.Event.ParticipationAdded( participation ), study.consumeEvents().last() )
    }

    @Test
    fun addParticipation_fails_when_study_not_live()
    {
        val study = createStudy()
        setDeployableProtocol( study )

        assertFalse( study.canDeployToParticipants )

        val participation = DeanonymizedParticipation( UUID.randomUUID(), Participation( UUID.randomUUID() ) )
        assertFailsWith<IllegalStateException> { study.addParticipation( participation ) }
        val participationEvents = study.consumeEvents().filterIsInstance<Study.Event.ParticipationAdded>()
        assertEquals( 0, participationEvents.count() )
    }

    private fun createStudy(): Study = Study( StudyOwner(), "Test study" )

    private fun setDeployableProtocol( study: Study )
    {
        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
        protocol.addMasterDevice( Smartphone( "User's phone" ) ) // One master device is needed to deploy.
        study.protocolSnapshot = protocol.getSnapshot()
    }
}
