package dk.cachet.carp.studies.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.studies.domain.users.StudyOwner
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
        assertEquals( name, foundStudy.invitation.name ) // Default study description when not specified.
        assertFalse( foundStudy.canDeployParticipants )
    }

    @Test
    fun createStudy_with_invitation_succeeds() = runBlockingTest {
        val ( service, repo ) = createService()

        val owner = StudyOwner()
        val name = "Test"
        val invitation = StudyInvitation( "Lorem ipsum" )
        val status = service.createStudy( owner, name, invitation )

        val foundStudy = repo.getById( status.studyId )!!
        assertEquals( status.studyId, foundStudy.id )
        assertEquals( name, foundStudy.name )
        assertEquals( invitation, foundStudy.invitation )
        assertFalse( foundStudy.canDeployParticipants )
    }

    @Test
    fun getStudyStatus_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val status = service.createStudy( StudyOwner(), "Test" )

        val foundStatus = service.getStudyStatus( status.studyId )
        assertEquals( status, foundStatus )
    }

    @Test
    fun getStudyStatus_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        assertFailsWith<IllegalArgumentException> { service.getStudyStatus( UUID.randomUUID() ) }
    }

    @Test
    fun getStudiesOverview_returns_owner_studies() = runBlockingTest {
        val ( service, _ ) = createService()
        val owner = StudyOwner()
        val studyOne = service.createStudy( owner, "One" )
        val studyTwo = service.createStudy( owner, "Two" )
        service.createStudy( StudyOwner(), "Three" )

        val studiesOverview = service.getStudiesOverview( owner )
        val expectedStudies = listOf( studyOne, studyTwo )
        assertEquals( 2, studiesOverview.intersect( expectedStudies ).count() )
    }

    @Test
    fun adding_and_retrieving_participant_succeeds() = runBlockingTest {
        val ( service, _ ) = createService()
        val owner = StudyOwner()
        val study = service.createStudy( owner, "Test" )
        val studyId = study.studyId

        val participant = service.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val studyParticipants = service.getParticipants( studyId )
        assertEquals( participant, studyParticipants.single() )
    }

    @Test
    fun addParticipant_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        val unknownId = UUID.randomUUID()
        val email = EmailAddress( "test@test.com" )
        assertFailsWith<IllegalArgumentException> { service.addParticipant( unknownId, email ) }
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipant_twice_returns_same_participant() = runBlockingTest {
        val ( service, _ ) = createService()
        val study = service.createStudy( StudyOwner(), "Test" )
        val studyId = study.studyId

        val email = EmailAddress( "test@test.com" )
        val p1 = service.addParticipant( studyId, email )
        val p2 = service.addParticipant( studyId, email )
        assertTrue( p1 == p2 )
    }

    @Test
    fun getParticipants_fails_for_unknown_studyId() = runBlockingTest {
        val ( service, _ ) = createService()

        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { service.getParticipants( unknownId ) }
    }
}
