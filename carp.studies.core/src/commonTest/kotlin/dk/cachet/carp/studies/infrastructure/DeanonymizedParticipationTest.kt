package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.users.DeanonymizedParticipation
import kotlin.test.*


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
