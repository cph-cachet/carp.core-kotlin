package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [ParticipationRepository].
 */
interface ParticipationRepositoryTest
{
    /**
     * Called for each test to create a repository to run tests on.
     */
    fun createRepository(): ParticipationRepository


    @Test
    fun addInvitation_and_retrieving_it_succeeds() = runSuspendTest {
        val repo = createRepository()

        val account = Account.withUsernameIdentity( "test" )
        val participation = Participation( UUID.randomUUID() )
        val invitation = ParticipationInvitation( participation, StudyInvitation.empty(), setOf( "Test device" ) )
        repo.addInvitation( account.id, invitation )
        val retrievedInvitations = repo.getInvitations( account.id )
        assertEquals( invitation, retrievedInvitations.single() )
    }

    @Test
    fun getInvitations_is_empty_when_no_invitations() = runSuspendTest {
        val repo = createRepository()

        val invitations = repo.getInvitations( UUID.randomUUID() )
        assertEquals( 0, invitations.count() )
    }
}
