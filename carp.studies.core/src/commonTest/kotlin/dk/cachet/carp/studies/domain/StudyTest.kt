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
    fun canDeployParticipants_false_when_protocol_not_defined()
    {
        val study = createStudy()
        assertFalse( study.canDeployParticipants )
    }

    @Test
    fun canDeployParticipants_true_once_protocol_is_defined()
    {
        val study = createStudy()
        val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
        protocol.addMasterDevice( Smartphone( "User's phone" ) ) // One master device is needed to deploy.
        study.protocolSnapshot = protocol.getSnapshot()
        assertTrue( study.canDeployParticipants )
    }


    private fun createStudy(): Study = Study( StudyOwner(), "Test study" )
}
