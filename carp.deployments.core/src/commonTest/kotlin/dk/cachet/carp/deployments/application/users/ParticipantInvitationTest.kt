package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterDeviceProtocol
import kotlin.test.Test
import kotlin.test.assertFailsWith


/**
 * Tests for [ParticipantInvitation] and helper methods.
 */
class ParticipantInvitationTest
{
    private val participantId = UUID.randomUUID()
    private val identity: AccountIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
    private val invitation: StudyInvitation = StudyInvitation.empty()


    @Test
    fun throwIfInvalid_for_valid_invitations()
    {
        val deviceRoleName = "Test device"
        val protocol = createSingleMasterDeviceProtocol( deviceRoleName ).getSnapshot()
        val invitation = ParticipantInvitation( participantId, setOf( deviceRoleName ), identity, invitation )

        protocol.throwIfInvalid( listOf( invitation ) )
    }

    @Test
    fun throwIfInvalid_throws_for_empty_invitations()
    {
        val protocol = createSingleMasterDeviceProtocol().getSnapshot()

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalid( emptyList() ) }
    }

    @Test
    fun throwIfInvalid_throws_for_invalid_master_device()
    {
        val protocol = createSingleMasterDeviceProtocol( "Master" ).getSnapshot()
        val invitation = ParticipantInvitation( participantId, setOf( "Invalid" ), identity, invitation )

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalid( listOf( invitation ) ) }
    }

    @Test
    fun throwIfInvalid_throws_for_unassigned_master_device()
    {
        val toAssign = "Test device"
        val protocol = createEmptyProtocol().apply {
            addMasterDevice( StubMasterDeviceDescriptor( toAssign ) )
            addMasterDevice( StubMasterDeviceDescriptor( "Unassigned second device" ) )
        }.getSnapshot()
        val invitation = ParticipantInvitation( participantId, setOf( toAssign ), identity, invitation )

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalid( listOf( invitation ) ) }
    }
}
