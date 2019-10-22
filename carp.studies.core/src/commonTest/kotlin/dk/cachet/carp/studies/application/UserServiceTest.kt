package dk.cachet.carp.studies.application

import dk.cachet.carp.common.*
import dk.cachet.carp.studies.domain.Participant
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for implementations of [UserService].
 */
interface UserServiceTest
{
    fun createUserService(): UserService


    @Test
    fun createParticipant_has_matching_studyId() = runBlockingTest {
        val service: UserService = createUserService()
        val studyId = UUID.randomUUID()

        val participant = service.createParticipant( studyId, EmailAddress( "test@test.com" ) )

        assertEquals( studyId, participant.studyId )
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun createParticipant_with_same_studyId_and_email_returns_same_participant() = runBlockingTest {
        val service: UserService = createUserService()
        val studyId = UUID.randomUUID()
        val email = EmailAddress( "test@test.com" )

        val p1: Participant = service.createParticipant( studyId, email )
        val p2: Participant = service.createParticipant( studyId, email )

        assertTrue( p1.id == p2.id )
    }
}