package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.deployments.application.users.ParticipantData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [ParticipantData] relying on core infrastructure.
 */
class ParticipantDataTest
{
    @Test
    fun can_serialize_and_deserialize_using_JSON()
    {
        JSON = createTestJSON()

        val data = mapOf( CarpInputDataTypes.SEX to Sex.Male )
        val participantData = ParticipantData( UUID.randomUUID(), data )

        val serialized: String = JSON.encodeToString( participantData )
        val parsed: ParticipantData = JSON.decodeFromString( serialized )

        assertEquals( participantData, parsed )
    }
}
