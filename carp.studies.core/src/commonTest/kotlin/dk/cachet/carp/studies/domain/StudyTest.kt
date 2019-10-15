package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Tests for [Study].
 */
class StudyTest
{
    @Test
    fun creating_study_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val owner = StudyOwner()
        val id = UUID.randomUUID()
        val study = Study( owner, "Test study", id )

        val snapshot = study.getSnapshot()
        val fromSnapshot = Study.fromSnapshot( snapshot )

        assertEquals( id, fromSnapshot.id )
        assertEquals( owner, fromSnapshot.owner )
        assertEquals( "Test study", fromSnapshot.name )
    }
}