package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.CustomMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.AssignedMasterDevice
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [ActiveParticipationInvitation] relying on core infrastructure.
 */
class ActiveParticipationInvitationTest
{
    @Test
    fun can_serialize_and_deserialize_active_participation_invitation_using_JSON()
    {
        val json = createTestJSON()
        val masterDevice = StubMasterDeviceDescriptor()
        val invitation = ActiveParticipationInvitation(
            Participation( UUID.randomUUID() ),
            StudyInvitation.empty(),
            setOf( AssignedMasterDevice( masterDevice ) )
        )

        val serialized = json.encodeToString( invitation )
        val parsed = json.decodeFromString<ActiveParticipationInvitation>( serialized )

        assertEquals( invitation, parsed )
    }

    @Test
    fun serializing_unknown_master_device_removes_the_wrapper()
    {
        val json = createTestJSON()
        val masterDevice = StubMasterDeviceDescriptor()
        val masterDeviceJson = json.encodeToString( masterDevice )
        val unknownMasterDevice = CustomMasterDeviceDescriptor( "unknown.device", masterDeviceJson, json )
        val invitation = ActiveParticipationInvitation(
            Participation( UUID.randomUUID() ),
            StudyInvitation.empty(),
            setOf( AssignedMasterDevice( unknownMasterDevice ) )
        )

        val serialized = json.encodeToString( invitation )
        assertTrue( !serialized.contains( "CustomMasterDeviceDescriptor" ) )
    }
}
