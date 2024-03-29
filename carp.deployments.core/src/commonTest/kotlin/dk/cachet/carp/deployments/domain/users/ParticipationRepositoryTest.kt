package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.createComplexParticipantGroup
import dk.cachet.carp.deployments.domain.studyDeploymentFor
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import kotlinx.coroutines.test.runTest
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
    fun getParticipations_succeeds() = runTest {
        val repo = createRepository()
        val protocol: StudyProtocol = createSinglePrimaryDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        val group = ParticipantGroup.fromNewDeployment( deployment )

        // Add participation.
        val account = Account.withEmailIdentity( "test@test.com" )
        val participation = Participation( deployment.id )
        val invitation = StudyInvitation( "Some study" )
        group.addParticipation( account, invitation, participation, protocol.primaryDevices )
        repo.putParticipantGroup( group )

        val expectedInvitations = setOf(
            AccountParticipation(
                participation,
                protocol.primaryDevices.map { it.roleName }.toSet(),
                account.id,
                invitation
            )
        )
        val invitations = repo.getParticipationInvitations( account.id )
        assertEquals( expectedInvitations, invitations )
    }

    @Test
    fun getParticipations_is_empty_when_no_participations() = runTest {
        val repo = createRepository()

        val invitations = repo.getParticipationInvitations( unknownId )
        assertEquals( 0, invitations.count() )
    }

    @Test
    fun putParticipantGroup_and_retrieving_it_succeeds() = runTest {
        val repo = createRepository()

        val group = createComplexParticipantGroup()
        repo.putParticipantGroup( group )
        val retrieved = repo.getParticipantGroup( group.studyDeploymentId )

        assertNotSame( group, retrieved ) // Should be new object instance.
        assertNotNull( retrieved )
        assertEquals( group.getSnapshot(), retrieved.getSnapshot() ) // ParticipantGroup does not implement equals, but snapshot does.
    }

    @Test
    fun getParticipantGroup_is_null_when_not_found() = runTest {
        val repo = createRepository()

        assertNull( repo.getParticipantGroup( unknownId ) )
    }

    @Test
    fun getParticipantGroupList_succeeds() = runTest {
        val repo = createRepository()
        val protocol: StudyProtocol = createSinglePrimaryDeviceProtocol()

        val deployment1 = studyDeploymentFor( protocol )
        val group1 = ParticipantGroup.fromNewDeployment( deployment1 )
        repo.putParticipantGroup( group1 )

        val deployment2 = studyDeploymentFor( protocol )
        val group2 = ParticipantGroup.fromNewDeployment( deployment2 )
        repo.putParticipantGroup( group2 )

        val groups = repo.getParticipantGroupList( setOf( deployment1.id, deployment2.id ) )
        assertEquals( // ParticipantGroup does not implement equals, but snapshot does.
            arrayOf( group1, group2 ).map { it.getSnapshot() }.toSet(),
            groups.map { it.getSnapshot() }.toSet()
        )
    }

    @Test
    fun getParticipantGroupList_is_empty_when_no_matches() = runTest {
        val repo = createRepository()

        val groups = repo.getParticipantGroupList( setOf( unknownId ) )
        assertTrue( groups.isEmpty() )
    }

    @Test
    fun putParticipant_returns_the_previously_stored_group() = runTest {
        val repo = createRepository()
        val group = createComplexParticipantGroup()

        val noPrevious = repo.putParticipantGroup( group )
        assertNull( noPrevious )

        val previous = repo.putParticipantGroup( group )
        assertNotNull( previous )
        assertEquals( group.createdOn, previous.createdOn )
    }

    @Test
    fun removeParticipantGroups_succeeds() = runTest {
        val repo = createRepository()
        val group1 = createComplexParticipantGroup()
        val group2 = createComplexParticipantGroup()
        repo.putParticipantGroup( group1 )
        repo.putParticipantGroup( group2 )

        val groupIds = setOf( group1.studyDeploymentId, group2.studyDeploymentId )
        val removedIds = repo.removeParticipantGroups( groupIds )
        assertEquals( groupIds, removedIds )
        assertNull( repo.getParticipantGroup( group1.studyDeploymentId ) )
        assertNull( repo.getParticipantGroup( group2.studyDeploymentId ) )
    }

    @Test
    fun removeParticipantGroups_ignores_unknown_ids() = runTest {
        val repo = createRepository()
        val group = createComplexParticipantGroup()
        repo.putParticipantGroup( group )

        val deploymentIds = setOf( group.studyDeploymentId, unknownId )
        val removed = repo.removeParticipantGroups( deploymentIds )
        assertEquals( setOf( group.studyDeploymentId ), removed )
    }
}
