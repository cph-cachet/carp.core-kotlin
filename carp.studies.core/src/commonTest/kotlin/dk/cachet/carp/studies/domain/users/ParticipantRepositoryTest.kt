package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for implementations of [ParticipantRepository].
 */
interface ParticipantRepositoryTest
{
    fun createRepository(): ParticipantRepository


    @Test
    fun adding_participant_and_retrieving_it_succeeds() = runSuspendTest {
        val repo = createRepository()
        val studyId = UUID.randomUUID()

        val participant = Participant( AccountIdentity.fromUsername( "user" ) )
        repo.addParticipant( studyId, participant )
        val studyParticipants = repo.getParticipants( studyId )
        assertEquals( participant, studyParticipants.single() )
    }

    @Test
    fun addParticipant_fails_for_duplicate_participant_id() = runSuspendTest {
        val repo = createRepository()
        val studyId = UUID.randomUUID()
        val participant = Participant( AccountIdentity.fromUsername( "user" ) )
        repo.addParticipant( studyId, participant )

        assertFailsWith<IllegalArgumentException> { repo.addParticipant( studyId, participant ) }
    }
}
