package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
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
        val roleAssignment = AssignedParticipantRoles( participantId, AssignedTo.All )
        group.addParticipants( setOf( roleAssignment ) )

        assertEquals( participantId, group.participantIds.singleOrNull() )
    }

    @Test
    fun addParticipants_fails_when_already_deployed()
    {
        val group = StagedParticipantGroup()
        val participantId = UUID.randomUUID()
        val roleAssignment = AssignedParticipantRoles( participantId, AssignedTo.All )
        group.addParticipants( setOf( roleAssignment ) )
        group.markAsDeployed()

        val newParticipantId = UUID.randomUUID()
        val newRoleAssignment = AssignedParticipantRoles( newParticipantId, AssignedTo.All )
        assertFailsWith<IllegalStateException> { group.addParticipants( setOf( newRoleAssignment ) ) }
    }

    @Test
    fun updateParticipants_succeeds()
    {
        val group = StagedParticipantGroup()
        val participantId = UUID.randomUUID()
        val roleAssignment = AssignedParticipantRoles( participantId, AssignedTo.All )
        group.addParticipants( setOf( roleAssignment ) )

        val newParticipantId = UUID.randomUUID()
        val newRoleAssignment = AssignedParticipantRoles( newParticipantId, AssignedTo.All )
        group.updateParticipants( setOf( newRoleAssignment ) )

        assertEquals( newParticipantId, group.participantIds.singleOrNull() )
    }

    @Test
    fun updateParticipants_fails_when_already_deployed()
    {
        val group = StagedParticipantGroup()
        val participantId = UUID.randomUUID()
        val roleAssingment = AssignedParticipantRoles( participantId, AssignedTo.All )
        group.addParticipants( setOf( roleAssingment ) )
        group.markAsDeployed()

        val newParticipantId = UUID.randomUUID()
        val newRoleAssignment = AssignedParticipantRoles( newParticipantId, AssignedTo.All )
        assertFailsWith<IllegalStateException> { group.updateParticipants( setOf( newRoleAssignment ) ) }
    }

    @Test
    fun markAsDeployed_succeeds()
    {
        val group = StagedParticipantGroup()
        val participantId = UUID.randomUUID()
        val roleAssignment = AssignedParticipantRoles( participantId, AssignedTo.All )
        group.addParticipants( setOf( roleAssignment ) )
        assertFalse( group.isDeployed )

        group.markAsDeployed()

        assertTrue( group.isDeployed )
    }

    @Test
    fun markAsDeployed_fails_when_no_participants_are_added()
    {
        val group = StagedParticipantGroup()
        assertFailsWith<IllegalStateException> { group.markAsDeployed() }
    }
}
