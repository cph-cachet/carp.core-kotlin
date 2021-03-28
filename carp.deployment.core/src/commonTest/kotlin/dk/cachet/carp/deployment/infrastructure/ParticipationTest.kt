package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployment.domain.users.Participation
import kotlin.test.*


/**
 * Tests for [Participation] relying on core infrastructure.
 */
class ParticipationTest
{
    @Test
    fun can_serialize_and_deserialize_participation_using_JSON()
    {
        val participation = Participation( UUID.randomUUID() )

        val serialized = participation.toJson()
        val parsed = Participation.fromJson( serialized )

        assertEquals( participation, parsed )
    }
}
