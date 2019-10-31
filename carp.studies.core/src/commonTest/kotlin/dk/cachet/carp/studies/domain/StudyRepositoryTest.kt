package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import kotlin.test.*


/**
 * Tests for implementations of [StudyRepository].
 */
interface StudyRepositoryTest
{
    fun createStudyRepository(): StudyRepository


    @Test
    fun cant_add_study_with_id_that_already_exists()
    {
        val repo = createStudyRepository()
        val id = UUID.randomUUID()
        val study1 = Study( StudyOwner(), "Study 1", id )
        repo.add( study1 )

        val studyWithSameId = Study( StudyOwner(), "Study 2", id)
        assertFailsWith<IllegalArgumentException>
        {
            repo.add( studyWithSameId )
        }
    }

    @Test
    fun getById_succeeds()
    {
        val repo = createStudyRepository()
        val study = Study( StudyOwner(), "Study" )
        repo.add( study )

        val foundStudy = repo.getById( study.id )
        assertEquals( study, foundStudy )
    }

    @Test
    fun getById_null_when_not_found()
    {
        val repo = createStudyRepository()

        val foundStudy = repo.getById( UUID.randomUUID() )
        assertNull( foundStudy )
    }
}