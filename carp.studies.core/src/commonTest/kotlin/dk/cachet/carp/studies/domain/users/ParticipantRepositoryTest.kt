package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for implementations of [ParticipantRepository].
 */
interface ParticipantRepositoryTest
{
    fun createRepository(): ParticipantRepository


    @Test
    fun adding_recruitment_and_retrieving_it_succeeds() = runSuspendTest {
        val repo = createRepository()

        val studyId = UUID.randomUUID()
        val recruitment = Recruitment( studyId )
        repo.addRecruitment( recruitment )
        val retrieved = repo.getRecruitment( studyId )

        assertEquals( recruitment.getSnapshot(), retrieved?.getSnapshot() )
    }

    @Test
    fun addRecruitment_for_existing_studyId_fails() = runSuspendTest {
        val repo = createRepository()
        val recruitment = Recruitment( UUID.randomUUID() )
        repo.addRecruitment( recruitment )

        assertFailsWith<IllegalArgumentException> { repo.addRecruitment( recruitment ) }
    }

    @Test
    fun getRecruitment_returns_null_when_not_found() = runSuspendTest {
        val repo = createRepository()

        val unknownId = UUID.randomUUID()
        assertNull( repo.getRecruitment( unknownId ) )
    }

    @Test
    fun updateRecruitment_succeeds() = runSuspendTest {
        val repo = createRepository()
        val recruitment = Recruitment( UUID.randomUUID() )
        repo.addRecruitment( recruitment )

        // TODO: Add and verify modifications once recruitment can be modified.
        repo.updateRecruitment( recruitment )
        val updatedRecruitment = repo.getRecruitment( recruitment.studyId )
        assertNotNull( updatedRecruitment )
    }

    @Test
    fun updateRecruitment_for_nonexisting_studyId_fails() = runSuspendTest {
        val repo = createRepository()

        val unknownRecruitment = Recruitment( UUID.randomUUID() )
        assertFailsWith<IllegalArgumentException> { repo.updateRecruitment( unknownRecruitment ) }
    }

    @Test
    fun removeStudy_succeeds() = runSuspendTest {
        val repo = createRepository()
        val studyId = UUID.randomUUID()
        val recruitment = Recruitment( studyId )
        repo.addRecruitment( recruitment )

        val isRemoved = repo.removeStudy( studyId )

        assertTrue( isRemoved )
        assertNull( repo.getRecruitment( studyId ) )
    }

    @Test
    fun removeStudy_returns_false_when_study_not_present() = runSuspendTest {
        val repo = createRepository()

        val isRemoved = repo.removeStudy( UUID.randomUUID() )

        assertFalse( isRemoved )
    }
}
