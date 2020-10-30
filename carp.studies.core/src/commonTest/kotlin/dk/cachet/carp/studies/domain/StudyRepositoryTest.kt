package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for implementations of [StudyRepository].
 */
interface StudyRepositoryTest
{
    fun createRepository(): StudyRepository


    @Test
    fun cant_add_study_with_id_that_already_exists() = runBlockingTest {
        val repo = createRepository()
        val study = Study( StudyOwner(), "Test" )
        repo.add( study )

        val studyWithSameId = Study( StudyOwner(), "Study 2", "Description", StudyInvitation.empty(), study.id )
        assertFailsWith<IllegalArgumentException>
        {
            repo.add( studyWithSameId )
        }
    }

    @Test
    fun getById_succeeds() = runBlockingTest {
        val repo = createRepository()
        val study = Study( StudyOwner(), "Test" )
        repo.add( study )

        val foundStudy = repo.getById( study.id )
        assertNotSame( study, foundStudy ) // Should be new object instance.
        assertEquals( study.getSnapshot(), foundStudy?.getSnapshot() )
    }

    @Test
    fun getById_null_when_not_found() = runBlockingTest {
        val repo = createRepository()

        val foundStudy = repo.getById( UUID.randomUUID() )
        assertNull( foundStudy )
    }

    @Test
    fun getForOwner_returns_owner_studies_only() = runBlockingTest {
        val repo = createRepository()
        val owner = StudyOwner()
        val ownerStudy = Study( owner, "Test" )
        val wrongStudy = Study( StudyOwner(), "Test" )
        repo.add( ownerStudy )
        repo.add( wrongStudy )

        val ownerStudies = repo.getForOwner( owner )
        assertEquals( ownerStudy.id, ownerStudies.single().id )
    }

    @Test
    fun update_succeeds() = runBlockingTest {
        val repo = createRepository()
        val study = Study( StudyOwner(), "Test" )
        repo.add( study )

        study.name = "Changed name"
        study.description = "Changed description"
        val newInvitation = StudyInvitation( "Test name", "Test description" )
        study.invitation = newInvitation
        repo.update( study )
        val updatedStudy = repo.getById( study.id )
        assertNotNull( updatedStudy )
        assertEquals( "Changed name", updatedStudy.name )
        assertEquals( "Changed description", updatedStudy.description )
        assertEquals( newInvitation, updatedStudy.invitation )
    }

    @Test
    fun update_fails_for_unknown_study() = runBlockingTest {
        val repo = createRepository()

        val study = Study( StudyOwner(), "Test" )
        assertFailsWith<IllegalArgumentException> { repo.update( study ) }
    }
}
