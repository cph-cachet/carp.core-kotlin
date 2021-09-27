package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.*


/**
 * Tests for [StagedParticipantGroup].
 */
class StagedParticipantGroupTest
{
    @Test
    fun addParticipants_succeeds()
    {
        val group = StagedParticipantGroup()
        val participantId = UUID.randomUUID()
        group.addParticipants( setOf( participantId ) )

        assertEquals( participantId, group.participantIds.singleOrNull() )
    }

    @Test
    fun addParticipants_fails_when_already_deployed()
    {
        val group = StagedParticipantGroup()
        val participantId = UUID.randomUUID()
        group.addParticipants( setOf( participantId ) )
        val stubInvitedStatus =
            StudyDeploymentStatus.Invited( Clock.System.now(), UUID.randomUUID(), emptyList(), Clock.System.now() )
        group.markAsInvited( stubInvitedStatus )

        val newParticipantId = UUID.randomUUID()
        assertFailsWith<IllegalStateException> { group.addParticipants( setOf( newParticipantId ) ) }
    }

    @Test
    fun markAsInvited_succeeds()
    {
        val group = StagedParticipantGroup()
        val participantId = UUID.randomUUID()
        group.addParticipants( setOf( participantId ) )
        assertFalse( group.isDeployed )

        val expectedInvitedOn: Instant = Clock.System.now()
        val mockInvitedStatus =
            StudyDeploymentStatus.Invited( expectedInvitedOn, UUID.randomUUID(), emptyList(), Clock.System.now() )
        group.markAsInvited( mockInvitedStatus )

        assertEquals( expectedInvitedOn, group.invitedOn )
        assertTrue( group.isDeployed )
    }

    @Test
    fun markAsInvited_fails_when_no_participants_are_added()
    {
        val group = StagedParticipantGroup()
        val stubInvitedStatus =
            StudyDeploymentStatus.Invited( Clock.System.now(), UUID.randomUUID(), emptyList(), Clock.System.now() )
        assertFailsWith<IllegalStateException> { group.markAsInvited( stubInvitedStatus ) }
    }
}
