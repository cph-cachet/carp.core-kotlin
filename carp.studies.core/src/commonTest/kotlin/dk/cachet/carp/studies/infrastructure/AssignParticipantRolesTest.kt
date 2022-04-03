package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.users.AssignParticipantRoles
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [AssignParticipantRoles] which rely on core infrastructure.
 */
class AssignParticipantRolesTest
{
    @Test
    fun can_serialize_and_deserialize_assigned_participant_using_JSON()
    {
        val assign = AssignParticipantRoles( UUID.randomUUID(), AssignedTo.Anyone )

        val serialized: String = JSON.encodeToString( assign )
        val parsed: AssignParticipantRoles = JSON.decodeFromString( serialized )

        assertEquals( assign, parsed )
    }
}
