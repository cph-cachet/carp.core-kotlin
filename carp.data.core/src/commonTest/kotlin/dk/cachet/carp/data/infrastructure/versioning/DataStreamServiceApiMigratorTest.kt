package dk.cachet.carp.data.infrastructure.versioning

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.InterbeatInterval
import dk.cachet.carp.common.application.data.SensorData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.data.application.MutableDataStreamBatch
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.data.infrastructure.measurement
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.*


class DataStreamServiceApiMigratorTest
{
    @Test
    fun major_1_minor_0_to_1_getDataStream_removes_sensorSpecificData() = runTest {
        // Append a data point with sensorSpecificData to a stream.
        val service = InMemoryDataStreamService()
        val dataStreamId = DataStreamId( UUID.randomUUID(), "Test", CarpDataTypes.INTERBEAT_INTERVAL.type )
        val expectedDataStream = setOf(
            DataStreamsConfiguration.ExpectedDataStream( dataStreamId.deviceRoleName, dataStreamId.dataType )
        )
        val config = DataStreamsConfiguration( dataStreamId.studyDeploymentId, expectedDataStream )
        service.openDataStreams( config )
        val data = MutableDataStreamBatch().apply {
            val sequence = MutableDataStreamSequence<InterbeatInterval>( dataStreamId, 0, listOf( 1 ) ).apply {
                appendMeasurements( measurement( InterbeatInterval( sensorSpecificData = StubDataPoint() ), 0 ) )
            }
            appendSequence( sequence )
        }
        service.appendToDataStreams( dataStreamId.studyDeploymentId, data )

        // Create GetDataStream request object.
        val json = createTestJSON()
        val getDataStream = DataStreamServiceRequest.GetDataStream( dataStreamId, 0 )
        val getDataStreamJson = json.encodeToJsonElement( DataStreamServiceRequest.Serializer, getDataStream )
        assertTrue( getDataStreamJson is JsonObject )
        val getDataStreamJsonOld = JsonObject(
            getDataStreamJson.toMutableMap().apply {
                set( DataStreamServiceRequest<*>::apiVersion.name, JsonPrimitive( "1.0" ) )
            }
        )

        // Verify response to migrated request.
        val migratedRequest = DataStreamServiceApiMigrator.migrateRequest( json, getDataStreamJsonOld )
        val migratedResponse = migratedRequest.invokeOn( service ).toString()
        assertTrue( !migratedResponse.contains( SensorData::sensorSpecificData.name ) )
    }
}
