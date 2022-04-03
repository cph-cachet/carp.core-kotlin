package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.domain.users.getAssignedDeviceRoleNames
import dk.cachet.carp.deployments.infrastructure.InMemoryAccountService
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import kotlinx.coroutines.test.runTest
import kotlin.test.*


/**
 * Tests for [ParticipantGroupService].
 */
class ParticipantGroupServiceTest
{
    private lateinit var accountService: AccountService
    private lateinit var service: ParticipantGroupService

    private val protocol: StudyProtocol = createSinglePrimaryDeviceProtocol()
    val studyDeploymentId = UUID.randomUUID()

    @BeforeTest
    fun createServices()
    {
        accountService = InMemoryAccountService()
        service = ParticipantGroupService( accountService )
    }


    @Test
    fun createAndInviteParticipantGroup_succeeds() = runTest {
        val invitations = listOf( createParticipantInvitation() )
        val createdEvent = DeploymentService.Event.StudyDeploymentCreated(
            studyDeploymentId,
            protocol.getSnapshot(),
            invitations,
            connectedDevicePreregistrations = emptyMap()
        )
        val group = service.createAndInviteParticipantGroup( createdEvent )

        assertEquals( studyDeploymentId, group.studyDeploymentId )
        invitations.forEach { invitation ->
            val participation = group.participations.singleOrNull { it.participation.participantId == invitation.participantId }
            assertNotNull( participation )
            assertEquals( invitation.assignedRoles, participation.participation.assignedRoles )
            assertEquals( studyDeploymentId, participation.participation.studyDeploymentId )
        }
    }

    @Test
    fun createAndInviteParticipantGroup_creates_new_account_for_new_identity() = runTest {
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )

        val createdEvent = DeploymentService.Event.StudyDeploymentCreated(
            studyDeploymentId,
            protocol.getSnapshot(),
            listOf( createParticipantInvitation( emailIdentity ) ),
            connectedDevicePreregistrations = emptyMap()
        )
        service.createAndInviteParticipantGroup( createdEvent )

        // Verify whether account was added.
        val foundAccount = accountService.findAccount( emailIdentity )
        assertNotNull( foundAccount )
    }

    @Test
    fun createAndInviteParticipantGroup_with_multiple_participations_per_account_succeeds() = runTest {
        val device1Role = "Primary 1"
        val device2Role = "Primary 2"
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( StubPrimaryDeviceConfiguration( device1Role ) )
            addPrimaryDevice( StubPrimaryDeviceConfiguration( device2Role ) )
        }
        val identity = AccountIdentity.fromUsername( "Test" )
        val studyInvitation = StudyInvitation( "Some study" )
        val invitation1 = ParticipantInvitation( UUID.randomUUID(), AssignedTo.Anyone, identity, studyInvitation )
        val invitation2 = ParticipantInvitation( UUID.randomUUID(), AssignedTo.Anyone, identity, studyInvitation )

        val createdEvent = DeploymentService.Event.StudyDeploymentCreated(
            studyDeploymentId,
            protocol.getSnapshot(),
            listOf( invitation1, invitation2 ),
            connectedDevicePreregistrations = emptyMap()
        )
        val group = service.createAndInviteParticipantGroup( createdEvent )
        assertEquals( 2, group.participations.count() )
    }

    @Test
    fun createAndInviteParticipantGroup_fails_for_unknown_participant_role() = runTest {
        val erroneousInvitation = ParticipantInvitation(
            UUID.randomUUID(),
            AssignedTo.Roles( setOf( "Unknown role " ) ),
            AccountIdentity.fromUsername( "Test" ),
            StudyInvitation( "Some study" )
        )
        val createdEvent = DeploymentService.Event.StudyDeploymentCreated(
            studyDeploymentId,
            protocol.getSnapshot(),
            listOf( erroneousInvitation ),
            connectedDevicePreregistrations = emptyMap()
        )

        assertFailsWith<IllegalArgumentException> { service.createAndInviteParticipantGroup( createdEvent ) }
    }

    @Test
    fun getAssignedDeviceRoleNames_returns_all_primary_devices_for_anyone()
    {
        val unassignedDevice = StubPrimaryDeviceConfiguration( "One" )
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( unassignedDevice )
            addPrimaryDevice( StubPrimaryDeviceConfiguration( "Two", isOptional = true ) )
            addConnectedDevice( StubDeviceConfiguration(), unassignedDevice )
        }.getSnapshot()

        val assignedDevices = protocol.getAssignedDeviceRoleNames( AssignedTo.Anyone )
        assertEquals( setOf( "One", "Two" ), assignedDevices )
    }

    @Test
    fun getAssignedDeviceRoleNames_only_includes_devices_for_assigned_roles()
    {
        val role1Device = StubPrimaryDeviceConfiguration( "One" )
        val role1 = "Role 1"
        val role2Device = StubPrimaryDeviceConfiguration( "Two" )
        val role2 = "Role 2"
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( role1Device )
            addParticipantRole( ParticipantRole( role1, false ) )
            changeDeviceAssignment( role1Device, AssignedTo.Roles( setOf( role1 ) ) )
            addPrimaryDevice( role2Device )
            addParticipantRole( ParticipantRole( role2, false ) )
            changeDeviceAssignment( role2Device, AssignedTo.Roles( setOf( role2 ) ))
        }.getSnapshot()

        val role1Devices = protocol.getAssignedDeviceRoleNames( AssignedTo.Roles( setOf( role1 ) ) )
        assertEquals( setOf( "One" ), role1Devices )
        val role2Devices = protocol.getAssignedDeviceRoleNames( AssignedTo.Roles( setOf( role2 ) ) )
        assertEquals( setOf( "Two" ), role2Devices )
        val bothRolesDevices = protocol.getAssignedDeviceRoleNames( AssignedTo.Roles( setOf( role1, role2 ) ) )
        val allDeviceRoles = setOf( "One", "Two" )
        assertEquals( allDeviceRoles, bothRolesDevices )
        val anyRoleDevices = protocol.getAssignedDeviceRoleNames( AssignedTo.Anyone )
        assertEquals( allDeviceRoles, anyRoleDevices )
    }

    @Test
    fun getAssignedDeviceRoleNames_includes_devices_assigned_to_anyone_for_specific_roles()
    {
        val assignedDevice = StubPrimaryDeviceConfiguration( "One" )
        val role = "Role"
        val unassignedDevice = StubPrimaryDeviceConfiguration( "Two" )
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( assignedDevice )
            addParticipantRole( ParticipantRole( role, false ) )
            changeDeviceAssignment( assignedDevice, AssignedTo.Roles( setOf( role ) ) )
            addPrimaryDevice( unassignedDevice )
        }.getSnapshot()

        val roleDevices = protocol.getAssignedDeviceRoleNames( AssignedTo.Roles( setOf( role ) ) )
        assertEquals( setOf( "One", "Two" ), roleDevices )
    }

    @Test
    fun getAssignedDeviceRoleNames_fails_for_unknown_role()
    {
        val protocol = createSinglePrimaryDeviceProtocol().getSnapshot()

        assertFailsWith<IllegalArgumentException>
        {
            protocol.getAssignedDeviceRoleNames( AssignedTo.Roles( setOf( "Unknown" ) ) )
        }
    }
}
