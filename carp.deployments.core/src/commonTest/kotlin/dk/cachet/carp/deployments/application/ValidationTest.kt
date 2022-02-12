package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryWithConnectedDeviceProtocol
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
    fun throwIfInvalidInvitations_for_valid_invitations()
    {
        val deviceRoleName = "Test device"
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName ).getSnapshot()
        val invitation = createInvitation( setOf( deviceRoleName ) )

        protocol.throwIfInvalidInvitations( listOf( invitation ) )
    }

    @Test
    fun throwIfInvalidInvitations_for_valid_invitations_with_unassigned_optional_primary_device()
    {
        val toAssign = "Test device"
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( StubPrimaryDeviceConfiguration( toAssign ) )
            addPrimaryDevice( StubPrimaryDeviceConfiguration( "Unassigned optional device", true ) )
        }.getSnapshot()
        val invitation = createInvitation( setOf( toAssign ) )

        protocol.throwIfInvalidInvitations( listOf( invitation ) )
    }

    @Test
    fun throwIfInvalidInvitations_throws_for_empty_invitations()
    {
        val protocol = createSinglePrimaryDeviceProtocol().getSnapshot()

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalidInvitations( emptyList() ) }
    }

    @Test
    fun throwIfInvalidInvitations_throws_for_invalid_primary_device()
    {
        val protocol = createSinglePrimaryDeviceProtocol( "Primary" ).getSnapshot()
        val invitation = createInvitation( setOf( "Invalid" ) )

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalidInvitations( listOf( invitation ) ) }
    }

    @Test
    fun throwIfInvalidInvitations_throws_for_unassigned_primary_device()
    {
        val toAssign = "Test device"
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( StubPrimaryDeviceConfiguration( toAssign ) )
            addPrimaryDevice( StubPrimaryDeviceConfiguration( "Unassigned second device" ) )
        }.getSnapshot()
        val invitation = createInvitation( setOf( toAssign ) )

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalidInvitations( listOf( invitation ) ) }
    }

    @Test
    fun throwIfInvalidPreregistrations_for_valid_preregistrations()
    {
        val primaryRoleName = "Primary"
        val connectedRoleName = "Connected"
        val protocol = createSinglePrimaryWithConnectedDeviceProtocol( primaryRoleName, connectedRoleName ).getSnapshot()

        val preregistrations = mapOf(
            connectedRoleName to protocol.connectedDevices.first { it.roleName == connectedRoleName }.createRegistration()
        )

        protocol.throwIfInvalidPreregistrations( preregistrations )
    }

    @Test
    fun throwIfInvalidPreregistrations_throws_for_preregistration_for_nonconnected_devices()
    {
        val primaryRoleName = "Primary"
        val connectedRoleName = "Connected"
        val protocol = createSinglePrimaryWithConnectedDeviceProtocol( primaryRoleName, connectedRoleName ).getSnapshot()

        val preregistrations = mapOf(
            primaryRoleName to protocol.primaryDevices.first { it.roleName == primaryRoleName }.createRegistration()
        )

        assertFailsWith<IllegalArgumentException> {
            protocol.throwIfInvalidPreregistrations( preregistrations )
        }
    }

    @Test
    fun throwIfInvalidPreregistrations_throws_for_preregistration_for_unknown_devices()
    {
        val deviceRoleName = "Primary"
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName ).getSnapshot()

        val preregistrations = mapOf( "Unknown" to DefaultDeviceRegistration() )

        assertFailsWith<IllegalArgumentException> {
            protocol.throwIfInvalidPreregistrations( preregistrations )
        }
    }

    @Test
    fun throwIfInvalidPreregistrations_throws_for_invalid_preregistrations()
    {
        val primaryRoleName = "Primary"
        val connectedRoleName = "Connected"
        val protocol = createSinglePrimaryWithConnectedDeviceProtocol( primaryRoleName, connectedRoleName ).getSnapshot()

        val invalidRegistration =
            object : DeviceRegistration()
            {
                override val deviceId: String = "Invalid"
                override val deviceDisplayName: String? = null
            }
        val preregistrations = mapOf( connectedRoleName to invalidRegistration )

        assertFailsWith<IllegalArgumentException> {
            protocol.throwIfInvalidPreregistrations( preregistrations )
        }
    }
}
