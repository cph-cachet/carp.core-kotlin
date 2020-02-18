package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.users.AccountIdentity
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
        val participant = Participant( AccountIdentity.fromEmailAddress( "test@test.com" ) )

        val serialized: String = participant.toJson()
        val parsed: Participant = Participant.fromJson( serialized )

        assertEquals( participant, parsed )
    }
}
