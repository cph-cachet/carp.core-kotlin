package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import kotlin.test.*


/**
 * Tests for implementations of [StudyRepository].
 */
interface StudyRepositoryTest
{
    fun createRepository(): StudyRepository


    @Test
    fun cant_add_study_with_id_that_already_exists()
    {
        val repo = createRepository()
        val id = UUID.randomUUID()
        val study1 = Study( StudyOwner(), "Study 1", StudyInvitation.empty(), id )
        repo.add( study1 )

        val studyWithSameId = Study( StudyOwner(), "Study 2", StudyInvitation.empty(), id )
        assertFailsWith<IllegalArgumentException>
        {
            repo.add( studyWithSameId )
        }
    }

    @Test
    fun getById_succeeds()
    {
        val repo = createRepository()
        val study = Study( StudyOwner(), "Study" )
        repo.add( study )

        val foundStudy = repo.getById( study.id )
        assertEquals( study, foundStudy )
    }

    @Test
    fun getById_null_when_not_found()
    {
        val repo = createRepository()

        val foundStudy = repo.getById( UUID.randomUUID() )
        assertNull( foundStudy )
    }
}
