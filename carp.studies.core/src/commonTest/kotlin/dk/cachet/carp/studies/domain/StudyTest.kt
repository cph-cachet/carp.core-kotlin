package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.Participation
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
        assertEquals( study.invitation, fromSnapshot.invitation )
        assertEquals( study.creationDate, fromSnapshot.creationDate )
        assertEquals( study.protocolSnapshot, fromSnapshot.protocolSnapshot )
        assertEquals( study.isLive, fromSnapshot.isLive )
        assertEquals( study.participations, fromSnapshot.participations )
    }

    @Test
    fun set_protocol_succeeds()
    {
        val study = createStudy()

        setDeployableProtocol( study )
    }

    @Test
    fun set_protocol_to_null_succeeds()
    {
        val study = createStudy()
        study.protocolSnapshot = null
    }

    @Test
    fun set_protocol_fails_for_protocol_with_deployment_errors()
    {
        val study = createStudy()

        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
        assertFailsWith<IllegalArgumentException> { study.protocolSnapshot = protocol.getSnapshot() }
    }

    @Test
    fun verify_state_of_new_study()
    {
        val study = createStudy()
        assertFalse( study.canDeployToParticipants )
        assertFalse( study.isLive )

        val status = study.getStatus()
        assertEquals( study.canDeployToParticipants, status.canDeployToParticipants )
        assertEquals( study.isLive, status.isLive )
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
    }

    @Test
    fun goLive_can_be_called_more_than_once()
    {
        val study = createStudy()
        setDeployableProtocol( study )
        study.goLive()

        // Study already live, but should not fail.
        study.goLive()
    }

    @Test
    fun goLive_fails_when_no_protocol_set()
    {
        val study = createStudy()
        assertFailsWith<IllegalStateException> { study.goLive() }
    }

    @Test
    fun addParticipation_succeeds()
    {
        val study = createStudy()
        setDeployableProtocol( study )
        study.goLive()

        val participation = DeanonymizedParticipation( UUID.randomUUID(), Participation( UUID.randomUUID() ) )
        study.addParticipation( participation )
    }

    @Test
    fun addParticipation_fails_when_study_not_live()
    {
        val study = createStudy()
        setDeployableProtocol( study )

        val participation = DeanonymizedParticipation( UUID.randomUUID(), Participation( UUID.randomUUID() ) )
        assertFailsWith<IllegalStateException> { study.addParticipation( participation ) }
    }

    private fun createStudy(): Study = Study( StudyOwner(), "Test study" )

    private fun setDeployableProtocol( study: Study )
    {
        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
        protocol.addMasterDevice( Smartphone( "User's phone" ) ) // One master device is needed to deploy.
        study.protocolSnapshot = protocol.getSnapshot()
    }
}
