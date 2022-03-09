package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.StudyInvitation
import kotlinx.coroutines.test.runTest
import kotlin.test.*


/**
 * Tests for implementations of [StudyRepository].
 */
interface StudyRepositoryTest
{
    fun createRepository(): StudyRepository


    @Test
    fun cant_add_study_with_id_that_already_exists() = runTest {
        val repo = createRepository()
        val study = Study( UUID.randomUUID(), "Test" )
        repo.add( study )

        val studyWithSameId = Study( UUID.randomUUID(), "Study 2", "Description", StudyInvitation( "Some study" ), study.id )
        assertFailsWith<IllegalArgumentException>
        {
            repo.add( studyWithSameId )
        }
    }

    @Test
    fun getById_succeeds() = runTest {
        val repo = createRepository()
        val study = Study( UUID.randomUUID(), "Test" )
        repo.add( study )

        val foundStudy = repo.getById( study.id )
        assertNotSame( study, foundStudy ) // Should be new object instance.
        assertEquals( study.getSnapshot(), foundStudy?.getSnapshot() )
    }

    @Test
    fun getById_null_when_not_found() = runTest {
        val repo = createRepository()

        val foundStudy = repo.getById( UUID.randomUUID() )
        assertNull( foundStudy )
    }

    @Test
    fun getForOwner_returns_owner_studies_only() = runTest {
        val repo = createRepository()
        val ownerId = UUID.randomUUID()
        val ownerStudy = Study( ownerId, "Test" )
        val wrongStudy = Study( UUID.randomUUID(), "Test" )
        repo.add( ownerStudy )
        repo.add( wrongStudy )

        val ownerStudies = repo.getForOwner( ownerId )
        assertEquals( ownerStudy.id, ownerStudies.single().id )
    }

    @Test
    fun update_succeeds() = runTest {
        val repo = createRepository()
        val study = Study( UUID.randomUUID(), "Test" )
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
    fun update_fails_for_unknown_study() = runTest {
        val repo = createRepository()

        val study = Study( UUID.randomUUID(), "Test" )
        assertFailsWith<IllegalArgumentException> { repo.update( study ) }
    }

    @Test
    fun remove_succeeds() = runTest {
        val repo = createRepository()
        val study = Study( UUID.randomUUID(), "Test" )
        repo.add( study )

        val isRemoved = repo.remove( study.id )
        assertTrue( isRemoved )
        assertNull( repo.getById( study.id ) )
    }

    @Test
    fun remove_returns_false_when_study_not_present() = runTest {
        val repo = createRepository()
        val study = Study( UUID.randomUUID(), "Test")
        repo.add( study )
        repo.remove( study.id )

        val isRemoved = repo.remove( study.id )
        assertFalse( isRemoved )
    }
}
