package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Tests for [DeanonymizedParticipation] relying on core infrastructure.
 */
class DeanonymizedParticipationTest
{
    @Test
    fun can_serialize_and_deserialize_deanonymized_participation_using_JSON()
    {
        val participation = DeanonymizedParticipation( UUID.randomUUID(), UUID.randomUUID() )

        val serialized: String = JSON.encodeToString( DeanonymizedParticipation.serializer(), participation )
        val parsed: DeanonymizedParticipation = JSON.decodeFromString( DeanonymizedParticipation.serializer(), serialized )

        assertEquals( participation, parsed )
    }
}
