package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.deployments.domain.createComplexParticipantGroup
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
    fun getParticipations_succeeds() = runSuspendTest {
        val repo = createRepository()
        val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
        val deployment = StudyDeployment( protocol.getSnapshot() )
        val group = ParticipantGroup.fromNewDeployment( deployment )

        // Add participation.
        val account = Account.withEmailIdentity( "test@test.com" )
        val participation = Participation( deployment.id )
        val invitation = StudyInvitation( "Some study" )
        group.addParticipation( account, invitation, participation, protocol.masterDevices )
        repo.putParticipantGroup( group )

        val expectedInvitations = setOf(
            AccountParticipation(
                participation,
                protocol.masterDevices.map { it.roleName }.toSet(),
                account.id,
                invitation
            )
        )
        val invitations = repo.getParticipationInvitations( account.id )
        assertEquals( expectedInvitations, invitations )
    }

    @Test
    fun getParticipations_is_empty_when_no_participations() = runSuspendTest {
        val repo = createRepository()

        val invitations = repo.getParticipationInvitations( unknownId )
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
        val group1 = ParticipantGroup.fromNewDeployment( deployment1 )
        repo.putParticipantGroup( group1 )

        val deployment2 = StudyDeployment( protocol.getSnapshot() )
        val group2 = ParticipantGroup.fromNewDeployment( deployment2 )
        repo.putParticipantGroup( group2 )

        val groups = repo.getParticipantGroupList( setOf( deployment1.id, deployment2.id ) )
        assertEquals( // ParticipantGroup does not implement equals, but snapshot does.
            arrayOf( group1, group2 ).map { it.getSnapshot() }.toSet(),
            groups.map { it.getSnapshot() }.toSet()
        )
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

        val previous = repo.putParticipantGroup( group )
        assertNotNull( previous )
        assertEquals( group.createdOn, previous.createdOn )
    }

    @Test
    fun removeParticipantGroups_succeeds() = runSuspendTest {
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
    fun removeParticipantGroups_ignores_unknown_ids() = runSuspendTest {
        val repo = createRepository()
        val group = createComplexParticipantGroup()
        repo.putParticipantGroup( group )

        val deploymentIds = setOf( group.studyDeploymentId, unknownId )
        val removed = repo.removeParticipantGroups( deploymentIds )
        assertEquals( setOf( group.studyDeploymentId ), removed )
    }
}
