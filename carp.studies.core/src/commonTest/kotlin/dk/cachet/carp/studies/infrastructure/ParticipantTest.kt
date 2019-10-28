package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.users.Participant
import kotlin.test.*


/**
 * Tests for [Participant] relying on core infrastructure.
 */
class ParticipantTest
{
    @Test
    fun can_serialize_and_deserialize_participant_using_JSON()
    {
        val participant = Participant( UUID.randomUUID() )

        val serialized = participant.toJson()
        val parsed = Participant.fromJson( serialized )

        assertEquals( participant, parsed )
    }
}