package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.users.AssignParticipantDevice
import kotlin.test.*


/**
 * Tests for [AssignParticipantDevice] which rely on core infrastructure.
 */
class AssignParticipantDeviceTest
{
    @Test
    fun can_serialize_and_deserialize_assigned_participant_using_JSON()
    {
        val assign = AssignParticipantDevice( UUID.randomUUID(), "Test device" )

        val serialized: String = assign.toJson()
        val parsed: AssignParticipantDevice = AssignParticipantDevice.fromJson( serialized )

        assertEquals( assign, parsed )
    }
}
