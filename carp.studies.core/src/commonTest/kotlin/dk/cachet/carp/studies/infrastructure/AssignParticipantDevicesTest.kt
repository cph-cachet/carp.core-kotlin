package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

        val serialized: String = JSON.encodeToString( assign )
        val parsed: AssignParticipantDevices = JSON.decodeFromString( serialized )

        assertEquals( assign, parsed )
    }
}
