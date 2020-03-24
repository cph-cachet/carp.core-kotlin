package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.StudyOwner
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
        val study = addStudy( repo )

        val studyWithSameId = Study( StudyOwner(), "Study 2", "Description", StudyInvitation.empty(), study.id )
        assertFailsWith<IllegalArgumentException>
        {
            repo.add( studyWithSameId )
        }
    }

    @Test
    fun getById_succeeds()
    {
        val repo = createRepository()
        val study = addStudy( repo )

        val foundStudy = repo.getById( study.id )
        assertEquals( study.getSnapshot(), foundStudy?.getSnapshot() )
    }

    @Test
    fun getById_null_when_not_found()
    {
        val repo = createRepository()

        val foundStudy = repo.getById( UUID.randomUUID() )
        assertNull( foundStudy )
    }

    @Test
    fun getForOwner_returns_owner_studies_only()
    {
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
    fun update_succeeds()
    {
        val repo = createRepository()
        val study = Study( StudyOwner(), "Test" )
        repo.add( study )

        study.name = "Changed name"
        study.description = "Changed description"
        repo.update( study )
        val updatedStudy = repo.getById( study.id )
        assertNotNull( updatedStudy )
        assertEquals( "Changed name", updatedStudy.name )
        assertEquals( "Changed description", updatedStudy.description )
    }

    @Test
    fun update_fails_for_unknown_study()
    {
        val repo = createRepository()

        val study = Study( StudyOwner(), "Test" )
        assertFailsWith<IllegalArgumentException> { repo.update( study ) }
    }

    @Test
    fun adding_participant_and_retrieving_it_succeeds()
    {
        val repo = createRepository()
        val study = addStudy( repo )

        val participant = Participant( AccountIdentity.fromUsername( "user" ) )
        repo.addParticipant( study.id, participant )
        val studyParticipants = repo.getParticipants( study.id )
        assertEquals( participant, studyParticipants.single() )
    }

    @Test
    fun addParticipant_fails_with_nonexisting_studyId()
    {
        val repo = createRepository()

        val unknownId = UUID.randomUUID()
        val participant = Participant( AccountIdentity.fromUsername( "user" ) )
        assertFailsWith<IllegalArgumentException> { repo.addParticipant( unknownId, participant ) }
    }

    @Test
    fun addParticipant_fails_for_duplicate_participant_id()
    {
        val repo = createRepository()
        val study = addStudy( repo )
        val participant = Participant( AccountIdentity.fromUsername( "user" ) )
        repo.addParticipant( study.id, participant )

        assertFailsWith<IllegalArgumentException> { repo.addParticipant( study.id, participant ) }
    }

    @Test
    fun getParticipants_fails_for_nonexisting_studyId()
    {
        val repo = createRepository()

        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { repo.getParticipants( unknownId ) }
    }


    private fun addStudy( repo: StudyRepository ): Study
    {
        val study = Study( StudyOwner(), "Test")
        repo.add( study )
        return study
    }
}
