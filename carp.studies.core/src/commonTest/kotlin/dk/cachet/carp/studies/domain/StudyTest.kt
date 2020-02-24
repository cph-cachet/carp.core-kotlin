package dk.cachet.carp.studies.domain

import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.Smartphone
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
    }

    @Test
    fun set_protocol_succeeds()
    {
        val study = createStudy()

        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
        protocol.addMasterDevice( Smartphone( "User's phone" ) )
        study.protocolSnapshot = protocol.getSnapshot()
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
    fun canDeployParticipants_false_until_live()
    {
        val study = createStudy()
        assertFalse( study.canDeployToParticipants )

        // Define protocol.
        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
        protocol.addMasterDevice( Smartphone( "User's phone" ) ) // One master device is needed to deploy.
        study.protocolSnapshot = protocol.getSnapshot()
        assertFalse( study.canDeployToParticipants )

        // Go live.
        study.goLive()
        assertTrue( study.canDeployToParticipants )
    }

    @Test
    fun goLive_fails_when_no_protocol_set()
    {
        val study = createStudy()
        assertFailsWith<IllegalStateException> { study.goLive() }
    }


    private fun createStudy(): Study = Study( StudyOwner(), "Test study" )
}
