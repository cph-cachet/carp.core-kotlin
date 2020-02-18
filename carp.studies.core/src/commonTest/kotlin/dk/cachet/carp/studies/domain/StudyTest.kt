package dk.cachet.carp.studies.domain

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
    }
}
