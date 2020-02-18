package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import kotlin.test.*


/**
 * Tests for [Study].
 */
class StudyTest
{
    private fun createTestStudy(): Study
    {
        val owner = StudyOwner()
        val id = UUID.randomUUID()
        return Study( owner, "Test study", StudyInvitation.empty(), id )
    }

    @Test
    fun includeParticipant_succeeds()
    {
        val study: Study = createTestStudy()
        val participantId = UUID.randomUUID()

        study.includeParticipant( participantId )

        assertEquals( participantId, study.participantIds.single() )
    }

    @Test
    fun includeParticipant_multiple_times_only_adds_once()
    {
        val study: Study = createTestStudy()
        val participantId = UUID.randomUUID()

        study.includeParticipant( participantId )
        study.includeParticipant( participantId )

        assertEquals( participantId, study.participantIds.single() )
    }

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
        val commonParticipants = study.participantIds.intersect( fromSnapshot.participantIds )
        assertEquals( study.participantIds.count(), commonParticipants.count() )
    }
}
