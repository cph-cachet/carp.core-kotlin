package dk.cachet.carp.studies.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.StudyDescription
import dk.cachet.carp.studies.domain.StudyOwner
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for implementations of [StudyService].
 */
interface StudyServiceTest
{
    /**
     * Create a user service and repository it depends on to be used in the tests.
     */
    fun createService(): Pair<StudyService, StudyRepository>


    @Test
    fun createStudy_succeeds() = runBlockingTest {
        val ( service, repo ) = createService()

        val owner = StudyOwner()
        val name = "Test"
        val status = service.createStudy( owner, name )

        // Verify whether study was added to the repository.
        val foundStudy = repo.getById( status.studyId )
        assertNotNull( foundStudy )
        assertEquals( status.studyId, foundStudy.id )
        assertEquals( name, foundStudy.name )
        assertEquals( name, foundStudy.description.name ) // Default study description when not specified.
    }

    @Test
    fun createStudy_with_description_succeeds() = runBlockingTest {
        val ( service, repo ) = createService()

        val owner = StudyOwner()
        val name = "Test"
        val description = StudyDescription( "Lorem ipsum" )
        val status = service.createStudy( owner, name, description )

        val foundStudy = repo.getById( status.studyId )!!
        assertEquals( status.studyId, foundStudy.id )
        assertEquals( name, foundStudy.name )
        assertEquals( description, foundStudy.description )
    }

    @Test
    fun getStudyStatus_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        val foundStatus = service.getStudyStatus( status.studyId )
        assertEquals( status, foundStatus )
    }

    @Test
    fun getStudyStatus_fails_for_unknown_study_id() = runBlockingTest {
        val ( service, _ ) = createService()

        assertFailsWith<IllegalArgumentException>
        {
            service.getStudyStatus( UUID.randomUUID() )
        }
    }
}
