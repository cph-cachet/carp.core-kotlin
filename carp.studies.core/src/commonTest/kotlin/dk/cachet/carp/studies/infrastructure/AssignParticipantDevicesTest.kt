package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import kotlin.test.*


/**
 * Tests for [AssignParticipantDevices] which rely on core infrastructure.
 */
class AssignParticipantDevicesTest
{
    @Test
    fun can_serialize_and_deserialize_assigned_participant_using_JSON()
    {
        val assign = AssignParticipantDevices( UUID.randomUUID(), setOf( "Test device" ) )

        val serialized: String = assign.toJson()
        val parsed: AssignParticipantDevices = AssignParticipantDevices.fromJson( serialized )

        assertEquals( assign, parsed )
    }
}
