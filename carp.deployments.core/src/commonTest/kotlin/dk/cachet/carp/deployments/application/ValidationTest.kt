package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterDeviceProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterWithConnectedDeviceProtocol
import kotlin.test.*


/**
 * Tests for helper methods in Validation.kt.
 */
class ValidationTest
{
    private val participantId = UUID.randomUUID()
    private val identity: AccountIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
    private val invitation: StudyInvitation = StudyInvitation( "Some study" )

    private fun createInvitation( assignedDevices: Set<String> ) =
        ParticipantInvitation( participantId, assignedDevices, identity, invitation )


    @Test
    fun throwIfInvalid_for_valid_invitations()
    {
        val deviceRoleName = "Test device"
        val protocol = createSingleMasterDeviceProtocol( deviceRoleName ).getSnapshot()
        val invitation = createInvitation( setOf( deviceRoleName ) )

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
        val invitation = createInvitation( setOf( "Invalid" ) )

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
        val invitation = createInvitation( setOf( toAssign ) )

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalid( listOf( invitation ) ) }
    }

    @Test
    fun throwIfInvalid_for_valid_preregistrations()
    {
        val masterRoleName = "Master"
        val connectedRoleName = "Connected"
        val protocol = createSingleMasterWithConnectedDeviceProtocol( masterRoleName, connectedRoleName ).getSnapshot()

        val invitation = createInvitation( setOf( masterRoleName ) )
        val preregistrations = mapOf(
            connectedRoleName to protocol.connectedDevices.first { it.roleName == connectedRoleName }.createRegistration()
        )

        protocol.throwIfInvalid( listOf( invitation ), preregistrations )
    }

    @Test
    fun throwIfInvalid_throws_for_preregistration_for_nonconnected_devices()
    {
        val masterRoleName = "Master"
        val connectedRoleName = "Connected"
        val protocol = createSingleMasterWithConnectedDeviceProtocol( masterRoleName, connectedRoleName ).getSnapshot()

        val invitation = createInvitation( setOf( masterRoleName ) )
        val preregistrations = mapOf(
            masterRoleName to protocol.masterDevices.first { it.roleName == masterRoleName }.createRegistration()
        )

        assertFailsWith<IllegalArgumentException> {
            protocol.throwIfInvalid( listOf( invitation ), preregistrations )
        }
    }

    @Test
    fun throwIfInvalid_throws_for_preregistration_for_unknown_devices()
    {
        val deviceRoleName = "Master"
        val protocol = createSingleMasterDeviceProtocol( deviceRoleName ).getSnapshot()

        val invitation = createInvitation( setOf( deviceRoleName ) )
        val preregistrations = mapOf( "Unknown" to DefaultDeviceRegistration( "ID" ) )

        assertFailsWith<IllegalArgumentException> {
            protocol.throwIfInvalid( listOf( invitation ), preregistrations )
        }
    }

    @Test
    fun throwIfInvalid_throws_for_invalid_preregistrations()
    {
        val masterRoleName = "Master"
        val connectedRoleName = "Connected"
        val protocol = createSingleMasterWithConnectedDeviceProtocol( masterRoleName, connectedRoleName ).getSnapshot()

        val invalidRegistration = object : DeviceRegistration() { override val deviceId: String = "Invalid" }
        val invitation = createInvitation( setOf( masterRoleName ) )
        val preregistrations = mapOf( connectedRoleName to invalidRegistration )

        assertFailsWith<IllegalArgumentException> {
            protocol.throwIfInvalid( listOf( invitation ), preregistrations )
        }
    }
}
