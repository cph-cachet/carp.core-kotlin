package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.CustomMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.deployment.domain.users.ActiveParticipationInvitation
import dk.cachet.carp.deployment.domain.users.AssignedMasterDevice
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [ActiveParticipationInvitation] relying on core infrastructure.
 */
class ActiveParticipationInvitationTest
{
    @Test
    fun can_serialize_and_deserialize_active_participatoin_invitation_using_JSON()
    {
        val json = createDeploymentSerializer( STUBS_SERIAL_MODULE )
        val masterDevice = StubMasterDeviceDescriptor()
        val invitation = ActiveParticipationInvitation(
            Participation( UUID.randomUUID() ),
            StudyInvitation.empty(),
            setOf( AssignedMasterDevice( masterDevice, null ) )
        )

        val serialized = json.encodeToString( invitation )
        val parsed = json.decodeFromString<ActiveParticipationInvitation>( serialized )

        assertEquals( invitation, parsed )
    }

    @Test
    fun serializing_unknown_master_device_removes_the_wrapper()
    {
        val json = createDeploymentSerializer( STUBS_SERIAL_MODULE )
        val masterDevice = StubMasterDeviceDescriptor()
        val masterDeviceJson = json.encodeToString( masterDevice )
        val unknownMasterDevice = CustomMasterDeviceDescriptor( "unknown.device", masterDeviceJson, json )
        val invitation = ActiveParticipationInvitation(
            Participation( UUID.randomUUID() ),
            StudyInvitation.empty(),
            setOf( AssignedMasterDevice( unknownMasterDevice, null ) )
        )

        val serialized = json.encodeToString( invitation )
        assertTrue( !serialized.contains( "CustomMasterDeviceDescriptor" ) )
    }
}
