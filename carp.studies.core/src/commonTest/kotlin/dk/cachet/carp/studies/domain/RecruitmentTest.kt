package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import kotlin.test.*


/**
 * Tests for [Recruitment].
 */
class RecruitmentTest
{
    @Test
    fun creating_recruitment_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val recruitment = Recruitment( UUID.randomUUID() )
        val protocol = createEmptyProtocol()
        val invitation = StudyInvitation( "Test", "A study" )
        recruitment.readyForDeployment( protocol.getSnapshot(), invitation )
        recruitment.addParticipation( UUID.randomUUID(), DeanonymizedParticipation( UUID.randomUUID(), UUID.randomUUID() ) )

        val snapshot = recruitment.getSnapshot()
        val fromSnapshot = Recruitment.fromSnapshot( snapshot )

        assertEquals( recruitment.studyId, fromSnapshot.studyId )
        assertEquals( recruitment.studyProtocol, fromSnapshot.studyProtocol )
        assertEquals( recruitment.invitation, fromSnapshot.invitation )
        assertEquals( recruitment.participations, fromSnapshot.participations )
    }

    @Test
    fun addParticipation_succeeds()
    {
        val recruitment = Recruitment( UUID.randomUUID() )
        val protocol = createEmptyProtocol()
        recruitment.readyForDeployment( protocol.getSnapshot(), StudyInvitation.empty() )

        assertTrue( recruitment.canAddParticipations )

        val studyDeploymentId = UUID.randomUUID()
        val participation = DeanonymizedParticipation( UUID.randomUUID(), UUID.randomUUID() )
        recruitment.addParticipation( studyDeploymentId, participation )
        assertEquals( Recruitment.Event.ParticipationAdded( studyDeploymentId, participation ), recruitment.consumeEvents().last() )
        assertEquals( participation, recruitment.getParticipations( studyDeploymentId ).single() )
    }

    @Test
    fun addParticipation_fails_when_study_protocol_not_locked_in()
    {
        val recruitment = Recruitment( UUID.randomUUID() )

        assertFalse( recruitment.canAddParticipations )

        val participation = DeanonymizedParticipation( UUID.randomUUID(), UUID.randomUUID() )
        val studyDeploymentId = UUID.randomUUID()
        assertFailsWith<IllegalStateException> { recruitment.addParticipation( studyDeploymentId, participation ) }
        val participationEvents = recruitment.consumeEvents().filterIsInstance<Recruitment.Event.ParticipationAdded>()
        assertEquals( 0, participationEvents.count() )
    }

    @Test
    fun getParticipations_fails_for_unknown_studyDeploymentId()
    {
        val recruitment = Recruitment( UUID.randomUUID() )

        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { recruitment.getParticipations( unknownId ) }
    }
}
