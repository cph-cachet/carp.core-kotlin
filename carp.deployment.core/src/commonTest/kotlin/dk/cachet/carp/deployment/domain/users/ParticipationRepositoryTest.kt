package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.createComplexParticipantGroup
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterDeviceProtocol
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


private val unknownId = UUID.randomUUID()


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

        val invitations = repo.getInvitations( unknownId )
        assertEquals( 0, invitations.count() )
    }

    @Test
    fun putParticipantGroup_and_retrieving_it_succeeds() = runSuspendTest {
        val repo = createRepository()

        val group = createComplexParticipantGroup()
        repo.putParticipantGroup( group )
        val retrieved = repo.getParticipantGroup( group.studyDeploymentId )

        assertNotSame( group, retrieved ) // Should be new object instance.
        assertNotNull( retrieved )
        assertEquals( group.getSnapshot(), retrieved.getSnapshot() ) // ParticipantGroup does not implement equals, but snapshot does.
    }

    @Test
    fun getParticipantGroup_is_null_when_not_found() = runSuspendTest {
        val repo = createRepository()

        assertNull( repo.getParticipantGroup( unknownId ) )
    }

    @Test
    fun getParticipantGroupList_succeeds() = runSuspendTest {
        val repo = createRepository()
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()

        val deployment1 = StudyDeployment( protocol.getSnapshot() )
        val group1 = ParticipantGroup.fromDeployment( deployment1 )
        repo.putParticipantGroup( group1 )

        val deployment2 = StudyDeployment( protocol.getSnapshot() )
        val group2 = ParticipantGroup.fromDeployment( deployment2 )
        repo.putParticipantGroup( group2 )

        val groups = repo.getParticipantGroupList( setOf( deployment1.id, deployment2.id ) )
        assertEquals( setOf( group1, group2 ), groups.toSet() )
    }

    @Test
    fun getParticipantGroupList_is_empty_when_no_matches() = runSuspendTest {
        val repo = createRepository()

        val groups = repo.getParticipantGroupList( setOf( unknownId ) )
        assertTrue( groups.isEmpty() )
    }

    @Test
    fun putParticipant_returns_the_previously_stored_group() = runSuspendTest {
        val repo = createRepository()
        val group = createComplexParticipantGroup()

        val noPrevious = repo.putParticipantGroup( group )
        assertNull( noPrevious )

        val group2 = createComplexParticipantGroup()
        val previous = repo.putParticipantGroup( group2 )
        assertNotNull( previous )
        assertEquals( group.creationDate, previous.creationDate )
    }
}
